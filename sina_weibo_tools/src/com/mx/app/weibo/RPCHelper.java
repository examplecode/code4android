package com.mx.app.weibo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

import com.mx.app.weibo.util.MD5;

public class RPCHelper {

	//sina weibp 客户端的 key
	private final static String KEY = "5l0WXnhiY4pJ794KIJ7Rw5F45VXg9sjo";
	
	private final static String WM = "2447_1001";
	
	private final static String FROM = "10265010";
	
	private final static String CID = "android";
	
	private final static String UA = "generic_2.2_weibo_2.6.0 beta1_android";
	
	private final static String LOGIN_URL_FMT = "http://t.sina.cn/interface/f/ttt/v3/login.php?wm=%s&from=%s&c=%s&ua=%s&lang=1";
	
	
	private final static String LOOKUP_USER_WEIBO_URL_FMT = "http://t.sina.cn/interface/f/ttt/v3/getusermbloglist.php?gsid=%s&uid=%s&picsize=240&page=%d&pagesize=%d&c=%s&s=%s&from=%s&wm=%s&lang=1&ua=%s";
	
	//发布,转发，
	private final static String DEAL_WEIBO_URL_FMT = "http://t.sina.cn/interface/f/ttt/v3/dealmblog.php?gsid=%s&wm=%s&from=%s&c=%s&ua=%s&lang=1";
	
	//关注，取消关注
	private final static String DEAL_ATT_URL_FMT = "http://t.sina.cn/interface/f/ttt/v3/dealatt.php?uid=%s&s=%s&gsid=%s&c=%s&wm=%s&ua=%s&from=%s&act=%d&lang=1";
	//搜索用户
	private final static String SEARCH_USER_URL_FMT = "http://t.sina.cn/interface/f/ttt/v3/searchuser.php?gsid=%s&keyword=%s&page=%dpagesize=%d&c=%s&s=%s&from=%s&wm=%s&lang=1&ua=%s";
	//搜索内容
	private final static String SEARCH_CONTENT_URL_FMT = "http://t.sina.cn/interface/f/ttt/v3/searchmblog.php?gsid=%s&keyword=%s&picsize=240&page=%d&pagesize=%d&c=%s&s=%s&from=%s&wm=%s&lang=1&ua=%s";
	
	//查询粉丝及关注
	private final static String LOOK_UP_ATTENTION_URL_FMT = "http://t.sina.cn/interface/f/ttt/v3/attention.php?gsid=%s&cat=%d&uid=%s&sort=3&lastmblog=1&page=%d&pagesize=%d&c=%s&s=%s&from=%s&wm=%s&lang=1&ua=%s";
	
	private static RPCHelper sHelper = null;
	
	/**
	 * 表示一个用户
	 */
	public static class User {
		public String nick = null;
		public String gsid = null;
		public String uid = null;
		

		@Override
		public String toString() {
			return "nkck:" + nick + "\n"
					+ "gisd:" + gsid + "\n"
					+ "uid:" + uid + "\n";
		}
	}
	/**
	 * 表示一条微薄
	 */
	public static class Message {
		public String ownerUid = null;
		public String content = null;
		public String weiboid = null;
		public String ownerNickName = null;
		
		public String toString() {
			return "content:" + content + "\n"
			+ "ownerUid:" + ownerUid + "\n"
			+ "weiboid:" + weiboid + "\n"
			+ "ownerNickName:" + ownerNickName + "\n";
			
		}
	}
	 
	private RPCHelper() {

	}
	
	
	public static RPCHelper getInstance() {
		
		if(sHelper == null) {
			sHelper = new RPCHelper();
		}
		return sHelper;
	}
	
	//生成摘要
	private String makeDigestKey(String s) {
		String str = MD5.hexdigest((s + KEY).getBytes());
		StringBuffer strBuf = new StringBuffer();
		char[] arrayOfChar = str.toCharArray();

		strBuf.append(arrayOfChar[1]);
		strBuf.append(arrayOfChar[5]);
		strBuf.append(arrayOfChar[2]);
		strBuf.append(arrayOfChar[10]);
		strBuf.append(arrayOfChar[17]);
		strBuf.append(arrayOfChar[9]);
		strBuf.append(arrayOfChar[25]);
		strBuf.append(arrayOfChar[27]);

		return strBuf.toString();
	}
	
	
	public User login(String name, String passwd)
			throws ClientProtocolException, IOException, XmlPullParserException {
		
		String url = String.format(LOGIN_URL_FMT, WM, FROM, CID, URLEncoder.encode(UA));
		ArrayList paramList = new ArrayList();
		// user
		BasicNameValuePair pUser = new BasicNameValuePair("u", name);
		paramList.add(pUser);
		// passwd
		BasicNameValuePair pPasswd = new BasicNameValuePair("p", passwd);
		paramList.add(pPasswd);
		// client type
		BasicNameValuePair pCid = new BasicNameValuePair("c", CID);
		paramList.add(pCid);
		// The key digest
		String digest = makeDigestKey(name + passwd);
		BasicNameValuePair pDigest = new BasicNameValuePair("s", digest);
		paramList.add(pDigest);
		// ua
		BasicNameValuePair pUa = new BasicNameValuePair(
				"ua", UA);

		paramList.add(pUa);
		UrlEncodedFormEntity localUrlEncodedFormEntity = new UrlEncodedFormEntity(
				paramList, "UTF-8");

		HttpResponse response = postWithFormEncoder(url,localUrlEncodedFormEntity);
		return paseLoginResp(response);
	}
	
	
	private HttpResponse postWithFormEncoder(String url,UrlEncodedFormEntity forms) throws ClientProtocolException, IOException {
		HttpPost localHttpPost = new HttpPost(url);

		buildRequestHeader(localHttpPost);

		localHttpPost.setEntity(forms);

		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response =  httpclient.execute(localHttpPost);
		httpclient.execute(localHttpPost);
//		Log.i("test", "request>>>>>:" + url);
		Log.i("test", "response>>>>>:" + dumpResponseToString(response));
		return response;
	}
	
	private HttpResponse get(String url) throws ClientProtocolException, IOException {
		HttpGet httpGet = new HttpGet(url);
		buildRequestHeader(httpGet);
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response =  httpclient.execute(httpGet);
		Log.i("test", "request>>>>>:" + url);
		return response;
	}
	
	public void fllowUser(String gsid,String uid,String destUid) throws ClientProtocolException, IOException {
		String digest = makeDigestKey(uid);
		//uid=%s&s=%s&gsid=%s&c=%s&wm=%s&ua=%s&from=%s&act=%d&lang=1";
		String url =  String.format(DEAL_ATT_URL_FMT, destUid,digest,gsid,CID, WM,  URLEncoder.encode(UA),FROM,1);
		Log.i("test", "request url:" + url);
		get(url);
	}
	
	public void unfllowUser(String gsid,String uid,String destUid) throws ClientProtocolException, IOException {
		String digest = makeDigestKey(uid);
		//uid=%s&s=%s&gsid=%s&c=%s&wm=%s&ua=%s&from=%s&act=%d&lang=1";
		String url =  String.format(DEAL_ATT_URL_FMT, destUid,digest,gsid,CID, WM,  URLEncoder.encode(UA),FROM,2);
		get(url);
	}
	//获取关注我的粉丝
	public ArrayList<User> getMyFans(String gsid,String uid,int pageNum,int pageSize) throws ClientProtocolException, IOException, XmlPullParserException {
		String digest = makeDigestKey(uid);
		String url =  String.format(LOOK_UP_ATTENTION_URL_FMT, gsid, 1,uid,pageNum,pageSize,CID,digest,FROM, WM,  URLEncoder.encode(UA));
		HttpResponse response = get(url);
		return parseUserListResp(response);
	}
	
	//获取我关注的用户
	public ArrayList<User> getMyAttented(String gsid,String uid,int pageNum,int pageSize) throws ClientProtocolException, IOException, XmlPullParserException {
		String digest = makeDigestKey(uid);
		String url =  String.format(LOOK_UP_ATTENTION_URL_FMT, gsid, 0,uid,pageNum,pageSize,CID,digest,FROM, WM,  URLEncoder.encode(UA));
		HttpResponse response = get(url);
		return parseUserListResp(response);
	}
	
	public ArrayList<Message> searchWeibo(String gsid,String uid,String keywords,int pageNum, int pageSize) throws ClientProtocolException, IOException, XmlPullParserException {
		String digest = makeDigestKey(uid);
		String url =  String.format(SEARCH_CONTENT_URL_FMT, gsid, URLEncoder.encode(keywords),pageNum,pageSize,CID,digest,FROM, WM,  URLEncoder.encode(UA));
		HttpResponse response = get(url);
		return parseMessageListResp(response);
	}
	
	public  ArrayList<User> searchUser(String gsid,String uid,String keywords,int pageNum, int pageSize) throws ClientProtocolException, IOException, XmlPullParserException {
//		&keyword=%s&page=%dpagesize=%d&c=%s&s=%s&from=%s&wm=%s&lang=1&ua=%s";
		String digest = makeDigestKey(uid);
		String url =  String.format(SEARCH_USER_URL_FMT, gsid, URLEncoder.encode(keywords,"UTF-8"),pageNum,pageSize,CID,digest,FROM, WM,  URLEncoder.encode(UA));
		HttpResponse response = get(url);
		return parseUserListResp(response);
	}
	
	public  ArrayList<Message> loadUserWeibo(String gsid, String uid,String ownerUid,
			int pageNum, int pageSize) throws ClientProtocolException, IOException, XmlPullParserException {
		String url = String.format(LOOKUP_USER_WEIBO_URL_FMT, gsid, ownerUid,
				pageNum, pageSize, CID, makeDigestKey(uid), FROM, WM,
				URLEncoder.encode(UA));
		HttpResponse response = get(url);
		return parseMessageListResp(response);
	}
	
	public void postMessage(String gsid,String uid,String content) throws ClientProtocolException, IOException {
		String url = String.format(DEAL_WEIBO_URL_FMT, gsid, WM,FROM,CID,URLEncoder.encode(UA));
		ArrayList paramList = new ArrayList();
		
		//content
		BasicNameValuePair pContent = new BasicNameValuePair("content", content);
		paramList.add(pContent);
		
		// client type
		BasicNameValuePair pCid = new BasicNameValuePair("c", CID);
		paramList.add(pCid);
		// The key digest
		String digest = makeDigestKey(uid);
		BasicNameValuePair pDigest = new BasicNameValuePair("s", digest);
		paramList.add(pDigest);
		
		// ua
		BasicNameValuePair pUa = new BasicNameValuePair(
				"ua", UA);
		paramList.add(pUa);
		
		//act
		BasicNameValuePair pAct = new BasicNameValuePair(
				"act", "add");
		paramList.add(pAct);
		
		// from
		BasicNameValuePair pFrom = new BasicNameValuePair(
				"from", FROM);
		paramList.add(pFrom);
		
		//wm
		BasicNameValuePair pWm = new BasicNameValuePair(
				"wm", FROM);
		paramList.add(pWm);
		

		
		UrlEncodedFormEntity forms = new UrlEncodedFormEntity(
				paramList, "UTF-8");
		
		postWithFormEncoder(url, forms);
		
	}
	
	/**
	 * 转发一条微薄
	 * @param gsid 
	 * @param uid 当前用户id
	 * @param msgid 微薄id
	 * @param ownerUid 微薄属主id
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public void forwardMessage(String gsid,String uid,String content,String msgid,String ownerUid) throws ClientProtocolException, IOException {
		String url = String.format(DEAL_WEIBO_URL_FMT, gsid, WM,FROM,CID,URLEncoder.encode(UA));
		ArrayList paramList = new ArrayList();
		
		//content
		BasicNameValuePair pContent = new BasicNameValuePair("content", content);
		paramList.add(pContent);
		
		//message id
		BasicNameValuePair pMsgid = new BasicNameValuePair("id", msgid);
		paramList.add(pMsgid);
				
		// client type
		BasicNameValuePair pCid = new BasicNameValuePair("c", CID);
		paramList.add(pCid);
		// The key digest
		String digest = makeDigestKey(uid);
		BasicNameValuePair pDigest = new BasicNameValuePair("s", digest);
		paramList.add(pDigest);
		
		// rtkeepreason
		BasicNameValuePair pRtkeepreason = new BasicNameValuePair(
				"rtkeepreason", "0");
		paramList.add(pRtkeepreason);
		
		// ua
		BasicNameValuePair pUa = new BasicNameValuePair(
				"ua", UA);
		paramList.add(pUa);
		
		//act
		BasicNameValuePair pAct = new BasicNameValuePair(
				"act", "dort");
		paramList.add(pAct);
		
		//mblogid
		BasicNameValuePair pMbloguid = new BasicNameValuePair(
				"mbloguid", ownerUid);
		paramList.add(pMbloguid);
		
		// from
		BasicNameValuePair pFrom = new BasicNameValuePair(
				"from", FROM);
		paramList.add(pFrom);
		
		//wm
		BasicNameValuePair pWm = new BasicNameValuePair(
				"wm", FROM);
		paramList.add(pWm);
		
		UrlEncodedFormEntity forms = new UrlEncodedFormEntity(
				paramList, "UTF-8");
		
		postWithFormEncoder(url, forms);
		
	}
	
	private String dumpResponseToString(HttpResponse response)
			throws IOException {
		if (response != null && response.getStatusLine().getStatusCode() == 200) {
			byte[] body = EntityUtils.toByteArray(response.getEntity());
			if (isGzipContentEnc(response)) {
				body = unzipData(body);
			}
			String charset = EntityUtils
					.getContentCharSet(response.getEntity());
			if (charset == null) {
				charset = "UTF-8";
			}
			Log.d("test", "========== response =============");
			String content =  new String(body, charset);
			Log.d("test", content);
			return content;
		}
		return null; 
	}
	

	
	private ArrayList<User> parseUserListResp(HttpResponse response) throws IOException, XmlPullParserException {
		
		ArrayList<User> userList = new ArrayList<User>();
		String body = dumpResponseToString(response);
		StringReader reader = new StringReader(body);
		XmlPullParser xmlpull = Xml.newPullParser();
		xmlpull.setInput(reader);
		User user = null;
		int eventType = xmlpull.getEventType();  
		while(eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				String tagName = xmlpull.getName();
				if(tagName.equals("info")) {
					user = new User();
				} else if(tagName.equals("uid")) {
					while( (eventType = xmlpull.next()) != XmlPullParser.TEXT) ;
					if(user != null) {
							user.uid = xmlpull.getText();
					}
				} else if(tagName.equals("nick")) {
					while( (eventType = xmlpull.next()) != XmlPullParser.TEXT) ;
					if(user != null) {
						user.nick = xmlpull.getText();
					}
				}
			} else if(eventType == XmlPullParser.END_TAG) {
				String tagName = xmlpull.getName();
				if(tagName.equals("info")) {
					userList.add(user);
					user = null;
				}
			}
			eventType = xmlpull.next();  
		}
		return userList;
	}
	
	private ArrayList<Message> parseMessageListResp(HttpResponse response) throws IOException, XmlPullParserException {
		ArrayList<Message> msgList = new ArrayList<Message>();
		String body = dumpResponseToString(response);
		StringReader reader = new StringReader(body);
		XmlPullParser xmlpull = Xml.newPullParser();
		xmlpull.setInput(reader);
		Message msg = null;
		int eventType = xmlpull.getEventType();  
		while(eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				String tagName = xmlpull.getName();
				if(tagName.equals("mblog")) {
					msg = new Message();
				} else if(tagName.equals("uid")) {
					while( (eventType = xmlpull.next()) != XmlPullParser.TEXT) ;
					if(msg != null) {
						msg.ownerUid = xmlpull.getText();
					}
				} else if(tagName.equals("mblogid")) {
					while( (eventType = xmlpull.next()) != XmlPullParser.TEXT) ;
					if(msg != null) {
						msg.weiboid = xmlpull.getText();
					}
				} else if(tagName.equals("nick")) {
					while( (eventType = xmlpull.next()) != XmlPullParser.TEXT) ;
					if(msg != null) {
						msg.ownerNickName = xmlpull.getText();
					}
				} else if(tagName.equals("content")) {
					while( (eventType = xmlpull.next()) != XmlPullParser.TEXT) ;
					if(msg != null) {
						msg.content = xmlpull.getText();
					}
				}
			} else if(eventType == XmlPullParser.END_TAG) {
				String tagName = xmlpull.getName();
				if(tagName.equals("mblog")) {
					msgList.add(msg);
					msg = null;
				}
			}
			eventType = xmlpull.next();  
		}
		return msgList;
	}
	
	
	private User paseLoginResp(HttpResponse response) throws IOException, XmlPullParserException  {
		User user = new User();
		String body = dumpResponseToString(response);
		StringReader reader = new StringReader(body);
		XmlPullParser xmlpull = Xml.newPullParser();
		xmlpull.setInput(reader);
		int eventType = xmlpull.getEventType();  
		while(eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				String tagName = xmlpull.getName();
				if(tagName.equals("gsid")) {
					eventType = xmlpull.next();  
					if(eventType == XmlPullParser.TEXT) {
						user.gsid = xmlpull.getText();
					}
					
				} else if(tagName.equals("uid")) {
					eventType = xmlpull.next();  
					if(eventType == XmlPullParser.TEXT) {
						user.uid = xmlpull.getText();
					}
					
				} else if(tagName.equals("nick")) {
					eventType = xmlpull.next();  
					if(eventType == XmlPullParser.TEXT) {
						user.nick = xmlpull.getText();
					}
					
				}
			}
			eventType = xmlpull.next();  
		}
		return user;
	}
	
	private void buildRequestHeader(HttpRequestBase request)
	{
		request.setHeader("User-Agent", UA);
		request.addHeader("Accept-Encoding", "gzip,deflate");
	}
	

	
	/**
	 * 解压gzip格式数据
	 * @param in
	 * @return
	 */
	public static final byte[] unzipData(byte[] in) {
		ByteArrayInputStream bais = null;
		GZIPInputStream gis = null;
		ByteArrayOutputStream baos = null;
		byte[] buffer = new byte[1024];
		int loop = 0;
		try {
			bais = new ByteArrayInputStream(in);
			gis = new GZIPInputStream(bais);
			baos = new ByteArrayOutputStream();

			int read = 0;

			while ((read = gis.read(buffer)) != -1) {
				loop++;
				baos.write(buffer, 0, read);
				baos.flush();
			}
				
		}// of try
		catch (Exception ioe) {
			buffer = null;
			ioe.printStackTrace();
			baos = null;

		} finally {
			try {
				if (gis != null)
					gis.close();
				if (bais != null)
					bais.close();
				if (baos != null)
					baos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	   
		return baos.toByteArray();
	}
	
	/**
	 * 检测是否是gzip压缩数据
	 * @param response
	 * @return
	 */
	private boolean isGzipContentEnc(HttpResponse response) {
		String contentEnc = null;
		Header header = response.getEntity().getContentEncoding();
		if (header != null) {
			contentEnc = header.getValue();
		}
		if (contentEnc != null && contentEnc.toLowerCase().indexOf("gzip") >= 0) {
			return true;
		}
		return false;
	}
}

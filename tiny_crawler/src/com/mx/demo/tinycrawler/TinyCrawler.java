package com.mx.demo.tinycrawler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import android.text.TextUtils;
import android.util.Log;


public class TinyCrawler {
	
	final static String LOG_TAG = "CRAWLER";
	private final static int BUF_SIZE = 4096; 
	

	
	public static String mBaseDir = "/sdcard/data/";
	public boolean mDownloadImage = true;
	
	/**
	 * 一个估算的值,从文件开头到这个值内存有声明文件字符集的字符串
	 * 必须为偶数
	 */
	final static int CHARSET_DECLARE_BLOCK_SIZE = 500;
	//默认抓取线程数
	final int numWorkers = 8;
	
	//抓取队列最大数目
	final int capacity = 1000000;
	BlockingQueue<CrawlRequest> mToCrawlQueue = null;
	
	/**
	 * 记录已经抓取到的列表
	 */
	HashSet<String> fetchedList = new HashSet<String>();
	
//	private String mBaseDir = "/sdcard/data/";
	
	Object mLockObj = new Object();
	
	private static TinyCrawler sCrawler = null;
	
	
	private CrawlStatusNotifyer mNotifyer = null;
	
	final static int STATUS_RUNNING = 0x100;
	final static int STATUS_PAUSE = 0x102;
    final static int STATUS_FINISH = 0x1014;
    
    
    public final static int ERR_NONE = 0;
    public final static int ERR_CANCEL = 1;
    public final static int ERR_IOEXCETPINT = 2;
    public final static int ERR_UNKNOW = 3;
	public final static int ERR_DISK_ERROR = 4;
	
	int mStatus = STATUS_FINISH;
	
	int mError = ERR_NONE;
	
	boolean  mCancel = false;
	
	/**
	 * 用来解析出文本中的字符集的声明
	 */
	public static Pattern charsetPattern  = Pattern.compile("(encoding|charset)=\"?([-a-z0-9A-Z]*)\"?");
	
	/**
	 * 匹配网页中的资源 link,css,js,image etc
	 */
	public static Pattern resourcePattern = Pattern.compile("<[a-zA-Z]+\\s*(href|src)=[\"']?([^'\" ]*)[\"']?[^>]*>");
	
	public static TinyCrawler getInstance() {
		if(sCrawler == null) {
			sCrawler = new TinyCrawler();
		}
		return sCrawler;
	}
	private  TinyCrawler()  {
			
		mToCrawlQueue  = new ArrayBlockingQueue<CrawlRequest>(capacity);
		//启动抓取线程
		CrawlThread[] workers = new CrawlThread[numWorkers];
		for (int i=0; i<workers.length; i++) {
		    workers[i] = new CrawlThread();
		    workers[i].start();
		}
	}
	
	public TinyCrawler setCrawlNotifyer(CrawlStatusNotifyer notifyer) {
		mNotifyer = notifyer;
		return this;
	}
	
	
	public interface CrawlStatusNotifyer {
		public void onCrawlStart(String url ,String title);
		public void onCrawlPause();
		public void onCrawlResume();
		public void onCrawlStop(int err);
		public void noitfyCrawlProgress(int crawled,int queueSize);
	}
	
	/**
	 * 描述一个抓取请求
	 */
	class CrawlRequest {
		public String mUrl;
		//描述url的深度,可通过level限制抓取网页的深度
		public int mLevel;
		//资源的另存名称
		String saveAs = null;
		public boolean isSource = false; //是否是最初抓取的页面
		public boolean hasLogged = false; //是否已记录到抓取历史列表
		public CrawlRequest(String url, int level) {
			mUrl = url;
			mLevel = level;
		}
		@Override
		public boolean equals(Object o) {
			// TODO Auto-generated method stub
			CrawlRequest req = (CrawlRequest) o;
			return mUrl.equals(req.mUrl);
		}
	}
	class CrawlThread extends Thread {
	    public void run() {
	            while (true) { 
	                // Retrieve an integer; block if the queue is empty
	            	CrawlRequest req = null;
					try {
						System.out.println( "================ crawl " + this.getName() + " started ================");
						
						req = mToCrawlQueue.take();
						
						if(fetchedList.size() % 100 == 0) {
							Thread.sleep(2000);
							System.gc();
						}
						if(mNotifyer != null && mStatus == STATUS_RUNNING) {
							mNotifyer.noitfyCrawlProgress(fetchedList.size(), mToCrawlQueue.size());
						}
						

						
						synchronized (mLockObj) {
							if(mStatus == STATUS_PAUSE) {
								if(mNotifyer != null) {
									mNotifyer.onCrawlPause();
								}
								mLockObj.wait();
								if(mNotifyer != null) {
									mNotifyer.onCrawlResume();
								}
							}
						}
						
						if(mCancel) {
							cancel();
						} else {
							fetchWebContent(req);
						}
					}
					catch (java.io.FileNotFoundException e) {
						//XXX：容错设置，遇到错误也不取消抓取任务
//						 cancel(ERR_DISK_ERROR);
						 e.printStackTrace();
					}
					 catch (IOException e) {
//						 mNotifyer.onCrawlStop(ERR_IOEXCETPINT);
//						 mStatus = STATUS_FINISH;
//						 cancel(ERR_DISK_ERROR);
//						 e.printStackTrace();
					 }
					 catch (Exception e) {
						// TODO Auto-generated catch block
//						 mNotifyer.onCrawlStop(ERR_UNKNOW);
						e.printStackTrace();
						
					} finally {
						if(mToCrawlQueue.size() == 0 && mNotifyer != null) {
							mNotifyer.onCrawlStop(mError);
							mStatus = STATUS_FINISH;
						}
					}
	            	System.out.println( "================== crawl " + this.getName() + "  down ==================");
	            }
	        } 
	}
	//发起一个抓取请求
	public void crawl(String url,int level,String title) throws InterruptedException,MalformedURLException, FileNotFoundException  {
		mCancel = false;
		if(/*mStatus == STATUS_FINISH && */mNotifyer != null) {
			mNotifyer.onCrawlStart(url,title);
		}
		mStatus = STATUS_RUNNING;
		CrawlRequest req = new CrawlRequest(url,level);
		req.isSource = true;
		if(!TextUtils.isEmpty(title)){
			logHistory(url,title);
			req.hasLogged=true;
		}
		req.saveAs = urlToLocalFile(url);
		mToCrawlQueue.put(req);

	}
	
	public void crawl(String url,String title) throws InterruptedException,MalformedURLException, FileNotFoundException {
		crawl(url,0,title);
	}
	
	public void pause() {
		mStatus = STATUS_PAUSE;
	}
	
    public void resume() {
    	synchronized (mLockObj) {
    		mLockObj.notifyAll();
    		mStatus = STATUS_RUNNING;
		}
	}
    
    public int getStatues() {
    	return mStatus;
    }
	
	public void cancel() {
		cancel(ERR_CANCEL);
	}
	
	private void cancel(int code) {
		mCancel = true;
		fetchedList.clear();
		mToCrawlQueue.clear();
		mStatus = STATUS_FINISH;
		mError = code;
		if(mNotifyer != null) {
			mNotifyer.onCrawlStop(code);
		}
	}
	/**
	 * 记录抓取请求记录
	 * @throws FileNotFoundException 
	 */
	private void logHistory(String url,String title) throws FileNotFoundException {
//		mCrawlLogger.println(title + '\t' + url);
	}
	
	
	private void buildRequestHedaer(HttpRequestBase request)
	{
		request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");  
//		request.setHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.3) Gecko/2008101315 Ubuntu/8.10 (intrepid) Firefox/3.0.3");
		request.setHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 2.1-update1; de-de; HTC Desire 1.19.161.5 Build/ERE27) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
		request.setHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		request.addHeader("Accept-Encoding", "gzip,deflate");
	}
	
	
	  private void saveFetchData(CrawlRequest req,byte[] data,String mimeType) throws IOException
	   {
		  File targetFile = null;
		  String saveAs = req.saveAs;
		  //没有后缀名称,自动根据mimeType加入后缀
		  if(saveAs.indexOf('.') < 0 ) {
			  {
				  if(isWebPageMimeType(mimeType)){
					  saveAs+=".html";
				  }
			  }
		  }
		  targetFile = new File(saveAs);
	      // create local directory
		  targetFile.getParentFile().mkdirs();
		  final FileOutputStream out = new FileOutputStream(targetFile);
		  out.write(data);
		  out.close();
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
		try {
			bais = new ByteArrayInputStream(in);
			gis = new GZIPInputStream(bais);
			baos = new ByteArrayOutputStream();
			int read = 0;
			while ((read = gis.read(buffer)) != -1) {
				baos.write(buffer, 0, read);
			}
			baos.flush();
				
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
	
	private void fetchWebContent(CrawlRequest req) throws ClientProtocolException, IOException ,InterruptedException
	{
		System.out.println( "crawled:["+ fetchedList.size() + "] " + "wait queue size:[" + mToCrawlQueue.size() + "]");
		System.out.println( "start crawl url:" + req.mUrl);
		HttpResponse response = null;
		String content = null;
		String contentType = null;
		String contentEnc = null;
		HttpClient httpClient =  new DefaultHttpClient();
		
		HttpGet method=new HttpGet(req.mUrl);
		 buildRequestHedaer(method);
		 response = httpClient.execute(method);
		
		if(response != null)
		{
			Header header = response.getEntity().getContentType();
			if(header != null)
			{
				contentType = header.getValue();
			}
			header = response.getEntity().getContentEncoding();
			if(header != null)
			{
				contentEnc = header.getValue();
			}
			byte[] contentData;
			contentData = readResponseData(response);
			
//			WeakReference wr = new WeakReference(contentData);
//			totalReceived +=contentData.length;
			String enc = detectEncoding(contentData, contentType);
			if(enc == null)
			{
				enc = "GBK"; //set to default
			}
			
			fetchedList.add(req.mUrl);
			
			//解压gzip数据
			if(contentEnc != null && contentEnc.indexOf("gzip")>=0)
			{
				contentData = unzipData(contentData);
			}
			if(isWebPageMimeType(contentType) && req.mLevel > 1) 
			{
				 content = new String(contentData,enc);
				 content = revertFetchLinks(req,content,contentType);			 //解析页面生成新的抓取请求和下载资源请求
					 
				 saveFetchData(req, content.getBytes(enc), contentType);
			} else 
			{
				saveFetchData(req, contentData, contentType);
			}
			contentData = null;
			
		}
	}
	
	/**
	 * 从给定的文本串中探测声明文本字符集
	 * @param text
	 * @return 返回该文本串声明的字符集编码名称
	 */
	public static String detectCharEncodingDeclare(String text)
	{
		Matcher matchText = charsetPattern.matcher(text);
		if(matchText.find())
		{
			return matchText.group(2);
		}
		
		return null;
	}
	
	/**
	 * 首先检测contentType是否有字符编码声明,如没有则从body的开头获取
	 * @param data
	 * @param contentType
	 * @return
	 */
	private String detectEncoding(byte[] data,String contentType)
	{
		String enc = null;
		enc = detectCharEncodingDeclare(contentType);
		if(enc == null)
		{
			String contentSec = null;
			int secSize = data.length > CHARSET_DECLARE_BLOCK_SIZE?CHARSET_DECLARE_BLOCK_SIZE:data.length;
			byte[] tmp = new byte[secSize]; //提高效率预先转换包含字符集编码信息的一段文本
			System.arraycopy(data, 0, tmp, 0,secSize);
			contentSec = new String(tmp);
			enc = detectCharEncodingDeclare(contentSec);
		}
		return enc;
	}
	
	
	private String revertFetchLinks(CrawlRequest currentReq,String content,String mimeType) throws MalformedURLException,InterruptedException, FileNotFoundException
	{
		if(currentReq.isSource && !currentReq.hasLogged){
			String regex = "<title.*?>(.*?)</title>";  
		    final Pattern pa = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);  
		    final Matcher ma = pa.matcher(content);
		    if(ma.find()){
		    	String title = ma.group(1);
		    	Log.i("crawl", title);
		    	if(TextUtils.isEmpty(title)){
		    		title = "Untitled";
		    	}
		    	logHistory(currentReq.mUrl, title);
		    }
		}
		
		StringBuffer result = new StringBuffer(content);
		
		int matchPointer = 0;
		//记录替换位置的指针,这里是android sdk的一个bug
		//通过这个变量来解决android sdk 正则的bug
		int replacePointer = 0;
		URL pageUrl =new URL(currentReq.mUrl);
		
		String matchString = null;
		int port = pageUrl.getPort();
		if(port <0) port = 80;
		Matcher m = resourcePattern.matcher(content);
		while (m.find(matchPointer)) {
			matchString = m.group();
			String targetLink =  m.group(2).trim();
			
			//完整的url
//			String fullUrl =targetLink;
			StringBuffer fullUrl = new StringBuffer();
			int offset = matchString.indexOf(targetLink);
			
			if (targetLink == null || targetLink.length() < 1) {
				matchPointer = m.end();
				continue;
			}
			// 跳过链到本页面内链接。
			if (targetLink.charAt(0) == '#') {
				matchPointer = m.end();
				continue;
			}
			if (targetLink.indexOf("mailto:") != -1) {
				matchPointer = m.end();
				continue;
			}
			if (targetLink.toLowerCase().indexOf("javascript") != -1) {
				matchPointer = m.end();
				continue;
			}
			if (targetLink.indexOf("://") == -1) {
				if (targetLink.charAt(0) == '/') {// 处理绝对地
					fullUrl.append("http://");
					fullUrl.append(pageUrl.getHost());
					fullUrl.append(':');
					fullUrl.append(port);
					fullUrl.append(targetLink);
//					fullUrl = "http://" + pageUrl.getHost() + ":"
//							+ port+ targetLink;
				} else {   //处理相对地址
					String path = pageUrl.getPath();
					fullUrl.append("http://");
					fullUrl.append(pageUrl.getHost());
					fullUrl.append(':');
					fullUrl.append(port);
					fullUrl.append(path);
					if (path.charAt(path.length() -1) != '/') fullUrl.append('/');
					fullUrl.append(targetLink);
				}
			} else {
				fullUrl.append(targetLink);
			}
			
			String toCrawlUrl = fullUrl.toString();
//			int index = targetLink.indexOf('#');
//			if (index != -1) {
//				
//				fullUrl = targetLink.substring(0, index);
//			}
			//更新指针重写缓冲区
			matchPointer = m.end();
			//android sdk 正则bug 通过记录lastOffset修正
			int start = m.start() + offset + replacePointer;
			int end = start  + targetLink.length();
			boolean fetched = fetchedList.contains(fullUrl);
			if(fetched) continue;  
			
			
			//一个页面链接
			if(matchString.indexOf("href") >0)
			{
				if(currentReq.mLevel > 1 && toCrawlUrl.startsWith("http://"))
				{
					//循环抓取
					CrawlRequest newReq = new CrawlRequest(toCrawlUrl,currentReq.mLevel -1);
//					newReq.path = currentReq.path + "/" + get
					newReq.saveAs = urlToLocalFile(toCrawlUrl);
					addToCrawQueue(newReq);
					
					//抓取深度大于1的时候 重写当前页面链接
//					String rewriteLink = "file://" + newReq.saveAs;
					String rewriteLink = fileNameToLocalUri(newReq.saveAs);
					result.replace(start , end, rewriteLink);
					replacePointer += rewriteLink.length() - targetLink.length();
					
				} else if(currentReq.mLevel == 1  && !targetLink.startsWith("http://")) { 
					String rewriteLink = fullUrl.toString();
					result.replace(start , end, rewriteLink);
					replacePointer += rewriteLink.length() - targetLink.length();
				}
			} else  {   //页面相关资源图片，CSS，JS等
				
				if(matchString.indexOf("img") > 0 ) {
//					System.out.println( "got image link url" +  fullUrl);
					if(!mDownloadImage) continue; //skip image
					
				} else if(matchString.indexOf("link") >0 ) {
					System.out.println( "got css link url" +  fullUrl);
				} else if(matchString.indexOf("script") > 0 ) {
					System.out.println( "got js link url" +  fullUrl);
				}
				String saveName = urlToLocalFile(toCrawlUrl);
				CrawlRequest newReq = new CrawlRequest(toCrawlUrl,0);
				newReq.saveAs = saveName;
				
				//重写URL
//				String rewriteLink = "file://" + newReq.saveAs;
				String rewriteLink = fileNameToLocalUri(newReq.saveAs);
				result.replace(start , end, rewriteLink);
				replacePointer += rewriteLink.length() - targetLink.length();
				addToCrawQueue(newReq);
			}
		}
		return result.toString();
	}
	
	private void addToCrawQueue(CrawlRequest req) throws InterruptedException {
		if(!mToCrawlQueue.contains(req)) {
			mToCrawlQueue.put(req);
		} 
	}
	
	
	byte[] readResponseData(HttpResponse response) throws IllegalStateException, IOException 
	{
		InputStream in = response.getEntity().getContent();

		int len = (int)response.getEntity().getContentLength();
		
		byte[] buf = new byte[BUF_SIZE];
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		 int readSize = 0;
		 int totalSize = 0;
         while ((readSize = in.read(buf)) >= 0) {
        	 totalSize += readSize;
 			out.write(buf, 0, readSize);
 			if(totalSize > 1024*1024) throw new IllegalStateException("too big size not suppost");
// 			out.flush();
 			if(totalSize == len ) break;
         }
//         out.flush();
         return  out.toByteArray();
         
	}
	
	/**
	 * 判断是否是Web页面类型的mimeType,用来检测当前
	 * @param mimeType
	 * @return
	 */
	private boolean isWebPageMimeType(String mimeType)
	{
		if(mimeType == null || mimeType.equals(""))
		{
			return false;
		} 
		if(mimeType.indexOf("text/html") >=0 )
		{
			return true;
		} else if(mimeType.indexOf("text/xhtml")>=0) {
			return true;
		}
			
		return false;
	}
	
	/**
	 * 判断指定的url是否有离线文件
	 * @param url
	 * @return
	 */
	public boolean hasOfflineFile(String url) {
		String localFile;
		try {
			localFile = urlToLocalFile(url);
			File file = new File(localFile);
			return file.exists();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public String urlToLocalFileUri(String url) throws MalformedURLException {
		StringBuffer buffer = new StringBuffer("file://");
//		buffer.append(URLEncoder.encode(urlToLocalFile(url)));
		buffer.append(urlToLocalFile(url));
		return buffer.toString();
				
	}
	
	private String fileNameToLocalUri(String fileNmae) {
		StringBuffer buffer = new StringBuffer("file://");
//		buffer.append(URLEncoder.encode(fileNmae));
		buffer.append(fileNmae);
		return buffer.toString();
	}
	
	  private String urlToLocalFile(String url) throws MalformedURLException {
		  StringBuffer buffer = new StringBuffer();
		  URL myUrl = new URL(url);
		  int port = myUrl.getPort();
		 
		  String strPort = "";
	      if (port != -1 && port != 80) {
	    	  strPort = "_" + port;
	      } 
		  String fileNameWithPath = myUrl.getFile();
		  String fileName = extractFileName(fileNameWithPath);
		  
		  String baseDir = mBaseDir;
		  if(baseDir.charAt(mBaseDir.length() -1) != '/') {
				baseDir +='/';
		 }
		  String path =  myUrl.getPath();
		  if(path == null || path.equals("")) path = "/";
		  if(fileName.matches("\\w*.(html|htm|js|css|png|gif|jpg)")) {
			  buffer.append(baseDir);
			  buffer.append(myUrl.getHost());
			  buffer.append(strPort);
			  buffer.append(fileNameWithPath);
//			  localpathname = mBaseDir + myUrl.getHost() + strPort + fileNameWithPath;
		  } else if(fileName == null || fileName.equals("")) {
			  fileName = "/index.html";
			  buffer.append(baseDir);
			  buffer.append(myUrl.getHost());
			  buffer.append(strPort);
			  buffer.append(fileName);
//			  localpathname = mBaseDir + myUrl.getHost() + strPort + path + fileName;
		  }
		  else {
			  //非正常动态资资源映射为 [hashCode].html
			  fileName = Integer.toHexString(url.hashCode())  + ".html";
			  buffer.append(baseDir);
			  buffer.append(myUrl.getHost());
			  buffer.append(strPort);
			  buffer.append(path);
			  if(path.charAt(path.length() -1) != '/') {
				  buffer.append('/');
			  }
			  buffer.append(fileName);
		  }
		  
		  return buffer.toString();
	  }
	  
		public  String extractFileName(String path) {

			if (path == null) {
				return null;
			}
			String newpath = path.replace('\\', '/');
			int start = newpath.lastIndexOf("/");
			if (start == -1) {
				start = 0;
			} else {
				start = start + 1;
			}
			String pageName = newpath.substring(start, newpath.length());

			return pageName;
		}
	
}

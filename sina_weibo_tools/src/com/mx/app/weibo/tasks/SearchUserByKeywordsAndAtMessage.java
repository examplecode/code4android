package com.mx.app.weibo.tasks;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mx.app.weibo.MxAsyncTaskRequest;
import com.mx.app.weibo.RPCHelper;
import com.mx.app.weibo.RPCHelper.Message;
import com.mx.app.weibo.RPCHelper.User;

public class SearchUserByKeywordsAndAtMessage extends MxAsyncTaskRequest {

	private Context mContext = null;
	private User mCurUser = null;
	private String mSearchKeywords= null;
	private final int LOOP_TIMES = 10;
	private final int DEFAULT_PAGESIZE = 10;
	
	private final String MESSAGE_FMT = "@%s 分享一个牛逼的网站 云端在线定制生成你想要的android浏览器，浏览器的主题，主页名称，图标甚至是布局 一切随你定制​! http://custom.maxthon.cn";
	public SearchUserByKeywordsAndAtMessage(Context context,User user,String searchKeywords,Handler handler, int taskId) {
		super(handler, taskId);
		// TODO Auto-generated constructor stub
		mContext = context;
		mCurUser = user;
		mSearchKeywords = searchKeywords;
	}

	@Override  
	protected void doTaskInBackground() {
		//读取上次的页码配置文件 
		int pageNum =  PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mCurUser.gsid + ".s_user_pn." + mSearchKeywords, 1);
		try {
			for(int i = 0 ; i < LOOP_TIMES; i++) {
				ArrayList<com.mx.app.weibo.RPCHelper.User> userList = RPCHelper.getInstance().searchUser(mCurUser.gsid, mCurUser.uid, mSearchKeywords, pageNum++, DEFAULT_PAGESIZE);
		    	Log.i("test", "total user:" + userList.size());
		    	if(userList.size() == 0) break;
		    	for(int j= 0; j< userList.size(); j++) {
		    		User user = userList.get(j);
		    		Log.i("test", "========= User =============");
		    		Log.i("test", user.toString());
		    		String m = String.format(MESSAGE_FMT, user.nick); 
		    		Log.i("test", m);  
		    		//关注此条微薄的主人
		    		RPCHelper.getInstance().postMessage(mCurUser.gsid, mCurUser.uid, m);
		    	}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt(mCurUser.gsid + ".s_user_pn.+ mSearchKeywords", pageNum).commit();
		}

	}

}

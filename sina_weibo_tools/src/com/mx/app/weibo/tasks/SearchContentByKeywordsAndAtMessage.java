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

public class SearchContentByKeywordsAndAtMessage extends MxAsyncTaskRequest {

	private Context mContext = null;
	private User mCurUser = null;
	private String mSearchKeywords= null;
	private final int LOOP_TIMES = 10;
	private final int DEFAULT_PAGESIZE = 10;
	
	private final String MESSAGE_FMT = "@%s 分享一个非常酷的网站 云端在线定制生成你想要的android浏览器，浏览器的主题，主页名称，图标甚至是布局 一切随你定制​. http://custom.maxthon.cn";
	public SearchContentByKeywordsAndAtMessage(Context context,User user,String searchKeywords,Handler handler, int taskId) {
		super(handler, taskId);
		// TODO Auto-generated constructor stub
		mContext = context;
		mCurUser = user;
		mSearchKeywords = searchKeywords;
	}

	@Override  
	protected void doTaskInBackground() {
		//读取上次的页码配置文件 
		int pageNum =  PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mCurUser.gsid + ".s_content_pn", 1);
		try {
			for(int i = 0 ; i < LOOP_TIMES; i++) {
				ArrayList<com.mx.app.weibo.RPCHelper.Message> weiboList = RPCHelper.getInstance().searchWeibo(mCurUser.gsid, mCurUser.uid, mSearchKeywords, pageNum++, DEFAULT_PAGESIZE);
		    	Log.i("test", "total user:" + weiboList.size());
		    	if(weiboList.size() == 0) break;
		    	for(int j= 0; j< weiboList.size(); j++) {
		    		Message msg = weiboList.get(j);
		    		Log.i("test", "========= Message =============");
		    		Log.i("test", msg.toString());
		    		String m = String.format(MESSAGE_FMT, msg.ownerNickName);
		    		Log.i("test", m);  
		    		//关注此条微薄的主人
		    		RPCHelper.getInstance().postMessage(mCurUser.gsid, mCurUser.uid, m);
		    	}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt(mCurUser.gsid + ".s_content_pn", pageNum).commit();
		}

	}

}

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

public class SearchContentByKeywordsAndFllow extends MxAsyncTaskRequest {

	private Context mContext = null;
	private User mCurUser = null;
	private String mSearchKeywords= null;
	private final int LOOP_TIMES = 10;
	private final int DEFAULT_PAGESIZE = 10;
	public SearchContentByKeywordsAndFllow(Context context,User user,String searchKeywords,Handler handler, int taskId) {
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
		    	for(int j= 0; j< weiboList.size(); j++) {
		    		Message msg = weiboList.get(j);
		    		Log.i("test", "========= Message =============");  
		    		Log.i("test", mCurUser.toString() );
		    		Log.i("test", msg.toString() );
		    		
		    		
		    		//关注此条微薄的主人
		    		RPCHelper.getInstance().fllowUser(mCurUser.gsid, mCurUser.uid, msg.ownerUid);
		    	
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

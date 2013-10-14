package com.mx.app.weibo.tasks;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mx.app.weibo.MxAsyncTaskRequest;
import com.mx.app.weibo.RPCHelper;
import com.mx.app.weibo.RPCHelper.User;

/**
 * 后台任务，搜索用户并且自动关注
 */
public class SearchUserByKeywordsAndFllow extends MxAsyncTaskRequest {
	private Context mContext = null;
	private User mCurUser = null;
	private String mSearchKeywords= null;
	private final int LOOP_TIMES = 10;
	private final int DEFAULT_PAGESIZE = 10;
	
	public SearchUserByKeywordsAndFllow(Context context,User user,String searchKeywords,Handler handler, int taskId) {
		super(handler, taskId);
		mContext = context;
		mCurUser = user;
		mSearchKeywords = searchKeywords;
	}

	@Override
	protected void doTaskInBackground() {
		//读取上次的页码配置文件 
		int pageNum =  PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mCurUser.gsid + ".s_user_pn", 1);
		ArrayList<User> userList = null;
		try {
			for(int i = 0 ; i < LOOP_TIMES; i++) {
				userList = RPCHelper.getInstance().searchUser(mCurUser.gsid, mCurUser.uid, mSearchKeywords, pageNum++, DEFAULT_PAGESIZE);
		    	Log.i("test", "total user:" + userList.size());
		    	for(int j= 0; j< userList.size(); j++) {
		    		User u = userList.get(j);
		    		Log.i("test", "========= user:" + u.nick + "=============");
		    		//关注用户
		    		RPCHelper.getInstance().fllowUser(mCurUser.gsid, mCurUser.uid, u.uid);
		    	}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt(mCurUser.gsid + ".s_user_pn", pageNum).commit();
		}

	}

}

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

public class UnfllowUserTask extends MxAsyncTaskRequest {

	private Context mContext = null;
	private User mCurUser = null;
	private final int LOOP_TIMES = 10;
	private final int DEFAULT_PAGESIZE = 10;
	public UnfllowUserTask(Context context,User user,Handler handler, int taskId) {
		super(handler, taskId);
		// TODO Auto-generated constructor stub
		mContext = context;
		mCurUser = user;
	}

	@Override
	protected void doTaskInBackground() {
		int pageNum = 1;
		try {
			for(int i = 0 ; i < LOOP_TIMES; i++) {
				ArrayList<User> userList =  RPCHelper.getInstance().getMyAttented(mCurUser.gsid, mCurUser.uid,pageNum, DEFAULT_PAGESIZE);
		    	Log.i("test", "total user:" + userList.size());
		    	for(int j= 0; i< userList.size(); j++) {
		    		User u = userList.get(j);
		    		Log.i("test", "========= user:" + u.nick + "=============");
		    		RPCHelper.getInstance().unfllowUser(mCurUser.gsid, mCurUser.uid, u.uid);
		    	}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
		}

	}

}

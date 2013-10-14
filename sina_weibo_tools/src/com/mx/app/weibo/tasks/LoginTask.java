package com.mx.app.weibo.tasks;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.mx.app.weibo.MxAsyncTaskRequest;
import com.mx.app.weibo.RPCHelper;

/**
 * 异步任务实现登陆
 */
public class LoginTask extends MxAsyncTaskRequest {
	private String mUserName = null;
	private String mPasswd = null;
	private Context mContext = null;
	public LoginTask(Context context,Handler handler, String userName,String passwd,int taskId) {
		super(handler, taskId);
		mContext = context;
		mUserName = userName;
		mPasswd = passwd;
	}

	@Override
	protected void doTaskInBackground() {
		try {
			com.mx.app.weibo.RPCHelper.User user = RPCHelper.getInstance().login(mUserName, mPasswd);
			if(user != null) {
				putExecuteResult(user);
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

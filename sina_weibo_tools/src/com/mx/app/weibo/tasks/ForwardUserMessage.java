package com.mx.app.weibo.tasks;

import android.os.Handler;

import com.mx.app.weibo.MxAsyncTaskRequest;

/**
 * 后台任务，搜索用户并且自动关注
 *
 */
public class ForwardUserMessage extends MxAsyncTaskRequest {

	public ForwardUserMessage(Handler handler, int taskId) {
		super(handler, taskId);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doTaskInBackground() {
		// TODO Auto-generated method stub

	}

}

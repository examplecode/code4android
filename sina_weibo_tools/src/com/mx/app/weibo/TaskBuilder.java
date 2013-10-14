package com.mx.app.weibo;

import android.content.Context;
import android.os.Handler;

/**
 * 类似于抽象工厂类,用于创建任务
 */
public interface TaskBuilder {

	public MxAsyncTaskRequest createTask(Context context,Handler h,int taskId);
}

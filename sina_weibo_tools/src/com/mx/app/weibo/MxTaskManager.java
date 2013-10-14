package com.mx.app.weibo;

import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.os.Handler;
import android.util.Log;


public class MxTaskManager {

	public final String LOG_TAG = "MxTaskManager";

	private static final int CORE_POOL_SIZE = 2;
	private static final int MAX_POOL_SIZE = 10;
	private static final int KEEP_ALIVE = 10;

	
	/**
	 * {@link TaskBuilder} 的实现用于创建task
	 */
	private TaskBuilder mTaskBuilderImpl = null;
	
	private Context mContext;
	//任务缓冲队列
	private final BlockingQueue<Runnable> sWorkQueue = new LinkedBlockingQueue<Runnable>(
			MAX_POOL_SIZE);
	//线程工场
	private final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			return new Thread(r, "MxAsyncTask #" + mCount.getAndIncrement());
		}
	};
	//工作线程池
	private final ThreadPoolExecutor sExecutor = new ThreadPoolExecutor(
			CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
			sWorkQueue, sThreadFactory);
	//已经在执行的任务－－用于取消任务
	private final Hashtable<Integer, TaskFuture> mFutureTasks = new Hashtable<Integer, TaskFuture>(CORE_POOL_SIZE);
	
	private class TaskFuture extends FutureTask<Void>{
		
		private MxAsyncTaskRequest mTask;

		public TaskFuture(final MxAsyncTaskRequest task) {
			super(new Callable<Void>(){
				public Void call() throws Exception {
					task.doTaskInBackground();
					return null;
				}
				
			});
			mTask = task;
		}
		
		@Override
        protected void done() {
			Log.v(LOG_TAG, "task["+mTask.getTaskId()+"] done");
			mTask.postExecute();
			mFutureTasks.remove(mTask.getTaskId());
		}
		
		@Override
		public void run(){
			Log.v(LOG_TAG, "task["+mTask.getTaskId()+"] going to start ...");
			mFutureTasks.put(mTask.getTaskId(), this);
			mTask.preExecute();
			super.run();
		}
	}
	

	private static MxTaskManager instance = null;


	static public MxTaskManager getInstance() {
		if (instance == null) {
			instance = new MxTaskManager();
		}
		return instance;
	}

	private MxTaskManager() {
	}
	

	/**
	 * 执行一个异步任务,此异步任务运行在另外一个线程
	 * 
	 * @param task
	 * @return 返回将被执行的任务句柄
	 */
	public MxAsyncTaskRequest executeTask(MxAsyncTaskRequest task) {
		if (task == null) {
			Log.e(LOG_TAG, "invalid task call.not allow null");
			return null;
		}
		if(task.canIgnoreIfExist()) {
			MxAsyncTaskRequest t = findTask(task.getTaskId());
			if(t!= null && task.canIgnoreIfExist()) {
				Log.i(LOG_TAG, "task has existed gnore :" + task.toString());
				return t;
			}
		}
		sExecutor.execute(new TaskFuture(task));
		
		Log.v(LOG_TAG, "PoolSize["+sExecutor.getPoolSize()+"]; ActiveCount["+sExecutor.getActiveCount()+"]; QueueSize["+sWorkQueue.size()+"]");
		
		return task;
//		StringBuffer sb = new StringBuffer();
//		for(Enumeration<TaskFuture> en = mFutureTasks.elements(); en.hasMoreElements();){
//			TaskFuture tf = en.nextElement();
//			if(null != tf && null != tf.mTask){
//				sb.append("running: ").append(tf.mTask.getTaskId()).append("\r\n");
//			}
//		}
//		for(Runnable tf : sWorkQueue){
//			TaskFuture t = (TaskFuture)tf;
//			if(null != t && null != t.mTask){
//				sb.append("waiting: ").append(t.mTask.getTaskId()).append("\r\n");
//			}
//		}
//		
//		Log.v(LOG_TAG, "pool dump:\r\n"+sb.toString());
	}
	

	/**
	 * 执行一个已经定义好的异步任务,给出任务ID的标识即可 此异步任务运行在后台无须和当前线程交互
	 * 
	 * @param taskId
	 */
//	public void excuteTask(int taskId) {
//		excuteTask(null,null,taskId);
//	}
	
	public void executeTask(int taskId)
	{
		executeTask(null,taskId);
	}

	/**
	 * 执行一个异步任务
	 * 
	 * @param h
	 *            来自当前线程,用于和异步任务进行交互
	 * @param taskId
	 *            任务ID
	 */
//	public void excuteTask(Handler h,int taskId)
//	{
//		excuteTask(null,h,taskId);
//	}
	
	public void executeTask(Handler h,int taskId) {
		if(mTaskBuilderImpl == null) {
			throw new IllegalStateException("task builder not install yet!");
		}
		MxAsyncTaskRequest task =  mTaskBuilderImpl.createTask(mContext,h, taskId);
		executeTask(task);
	}
	
	/**
	 * 安装初始化TaskManager,在初始化时调用
	 * @param context
	 * @param taskBuilderImpl
	 */
	public void setupTaskManager(Context context ,TaskBuilder taskBuilderImpl) {
		mContext = context;
		mTaskBuilderImpl = taskBuilderImpl;
	}

	/**
	 * 查找未执行完毕的任务
	 * 
	 * @param taskId
	 * @return null if no such task
	 */
	public MxAsyncTaskRequest findTask(int taskId) {
		MxAsyncTaskRequest task = null;
		for (Runnable r : sWorkQueue) {
			TaskFuture tr = (TaskFuture) r;		
//			TaskRunnable tr = (TaskRunnable) r;
			if (tr.mTask.getTaskId() == taskId) {
				task = tr.mTask;
				break;
			}
		}
		if(null == task){
			TaskFuture tf = mFutureTasks.get(taskId);
			if(null != tf){
				task = tf.mTask;
			}
		}	
		return task;
	}

	/**
	 * 尝试取消一个task 
	 * 如果该task在队列中等待执行， 则直接移除；
	 * 如果该task已经在执行， 则调用{@link MxAsyncTaskRequest#tryCancel()}来处理
	 * 如果{@link MxAsyncTaskRequest#tryCancel()}返回false， 并且<code>mayInterruptIfRunning</code>为<code>TRUE</code>, 则调用线程的interrupt方法
	 * 
	 * @param taskId
	 * @param mayInterruptIfRunning
	 */
	public boolean tryCancelTask(int taskId, boolean mayInterruptIfRunning) {
		MxAsyncTaskRequest fakeTask = new MxAsyncTaskRequest(null, taskId){
			@Override
			protected void doTaskInBackground() {
				//do nothing;
			}		
		};
		/**
		 * @see MxAsyncTaskRequest.equeals()
		 * @see LinkedBlockingQueue.remove(Object)
		 */
		while(sWorkQueue.remove(fakeTask));
		
		TaskFuture tf = mFutureTasks.get(taskId);
		if(null == tf)
			return true;
		
		MxAsyncTaskRequest task = tf.mTask;
		if (null != task) {
			if(task.tryCancel()){
				return true;
			}
			if(mayInterruptIfRunning){
				return tf.cancel(mayInterruptIfRunning);
			}
			return false;
		}
		return true;
	}

}


package com.mx.app.weibo;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public abstract class MxAsyncTaskRequest  {
	
	/**
	 * 用来标识异步任务请求消息的掩码.异步任务的消息ID在
	 * 此掩码的基础上递增
	 */
	public static final int ASYNC_TASK_MASK = 0x800000;
	
	/**
	 * Message 类的参数类型arg1 用来标识异步任务和
	 * 当前线程交互的消息
	 */
	static public final int TASK_STATUS_PENDING = -1;
	static public final int TASK_STATUS_START = 0;
	static public final int TASK_STATUS_FINISHED = 1;
	static public final int TASK_STATUS_UPDATE_PROGRESS = 2;
	static public final int TASK_STATUS_ERROR = 2;
	
	private int mStatus = TASK_STATUS_PENDING;
	
	/**
	 * 此标志被设置成true,当前如有相同的TaskId在执行
	 * 请求将被忽略
	 */
	protected boolean mIgnoreIfExist = true;
	
	/**
	 * 和当前线程交互的Handler
	 * 
	 */
	protected Handler mHandler = null;
	
	/**
	 * 存储异步任务的结果
	 */
	private Object mResult = null;
	
	/**
	 * 用来标识一个任务
	 */
	private int mTaskId = 0;
	/**
	 * 表示一个异步任务,该异步任务运行在独立的线程池(非当前线程)
	 * @param handler 一个和UI线程相关的Handle
	 * @param what 标识任务类型
	 */
	public MxAsyncTaskRequest(Handler handler,int taskId)
	{
		mHandler = handler;
		mTaskId = taskId;
	}
	
	public boolean canIgnoreIfExist() {
		return mIgnoreIfExist;
	}
	
	public int getTaskId() {
		return mTaskId;
	}
	
	public int getStatus(){
		return mStatus;
	}
	
	@Override
	public boolean equals(Object o){
		if(null == o) return false;
		
		if(o instanceof MxAsyncTaskRequest){
			return ((MxAsyncTaskRequest)o).getTaskId() == mTaskId;
		}
		
		return false;
	}
	
	
	/**
	 * 构造一个后台运行的异步任务,无需返回结果或和当前线程交互
	 */
	public MxAsyncTaskRequest(int taskId)
	{
		this(taskId,false);
	}
	
	/**
	 * 构造一个异步任务,指定是否在内部处理任务交互
	 * @param taskId
	 * @param handByself 
	 */
	public MxAsyncTaskRequest(int taskId,boolean handByself)
	{
		mTaskId = taskId;
		if(handByself) {
			mHandler = new Handler(Looper.myLooper()) {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					if (msg.what == mTaskId) {
						handTaskResult(msg.arg1, msg.arg2, msg.obj);
					}
				}
			};
		}
	}
	
	/**
	 * 当外部没有显式的传递handler过来(使用MxAsyncTaskRequest(taskid)构造task),task将会在此处理和任务的交互
	 * 注:此方法运行在当前线程用于和在异步线程中执行的任务进行交互
	 */
	protected void handTaskResult(int state,int progress,Object obj) {
		
	}
	
	/**
	 * 尝试取消当前任务。 
	 * 如果当前任务自行处理取消事件， 则在事件处理完成之后返回true。
	 * 如果当前任务取消失败， 或者由{@link MxTaskManager#tryCancelTask(int, boolean)}来处理取当前任务的消事件， 则返回false
	 * 
	 * @return：当前任务是否已经取消
	 */
	public boolean tryCancel(){
		return false;
	} 
	
	protected void onPreExecute(){

	}
	
	protected void onPostExecute(){

	}
	
	final void postExecute(){
		mStatus = TASK_STATUS_FINISHED;
		if(mHandler != null){
			//'100' means progress
			mHandler.obtainMessage(mTaskId, TASK_STATUS_FINISHED, 100, mResult).sendToTarget();
		}
		onPostExecute();
	}
	
	final void preExecute(){
		mStatus = TASK_STATUS_START;
		if(mHandler != null){
			mHandler.obtainMessage(mTaskId, TASK_STATUS_START, 0).sendToTarget();
		}
		onPreExecute();
	} 

	/**
	 * 执行耗时的异步任务,当前任务允许在线程池
	 * 不会阻塞UI线程
	 */
	abstract protected void doTaskInBackground();

	//================================= 以下方法应该在异步线程中被调用,用来通知请求线程(发起异步请求的线程)=========================//
	
	
	/**
	 * 保存异步任务的执行结果,任务结束后会把
	 * 执行结果递送到UI线程
	 * @param result
	 */
	final protected void putExecuteResult(Object result)
	{
		mResult = result;
	}
	
	/**
	 * 发送自定义消息给UI线程
	 * @param msgid
	 */
	public void sendMessage(int msgid)
	{
		sendMessage(msgid,null);
	}
	
	/**
	 * 发送消息给UI线程并携带数据信息
	 * @param msgid 用户自定义的消息
	 * @param data 由异步任务传递过来的数据
	 */
	public void sendMessage(int msgid,Object data)
	{
		if(mHandler == null)
		{
			throw new IllegalStateException("this task not associate with ui");
		}
		Message newMsg = Message.obtain(mHandler,mTaskId);
		newMsg.arg1 = msgid;
		newMsg.obj = data;
		newMsg.sendToTarget();
	}
	/**
	 * 发送进度消息给当前线程
	 * @param progress
	 */
	public void sendProgress(int progress)
	{
		mStatus = TASK_STATUS_UPDATE_PROGRESS;
		if(mHandler == null)
		{
			throw new IllegalStateException("this task not associate with ui");
		}
		Message newMsg = Message.obtain(mHandler,mTaskId);
		newMsg.arg1 = TASK_STATUS_UPDATE_PROGRESS;
		newMsg.arg2 = progress;
		newMsg.sendToTarget();
	}
	
}

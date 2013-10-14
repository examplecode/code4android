package com.mx.app.weibo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


/**
 * 接收广播消息并分发给监听广播的Listener
 */
public class BroadcastDispatcher {
	
	private Context mContext;

	private static final String LOGTAG = "BroadcastDispatcher";
	private static BroadcastDispatcher mDispatcher = null;
	/**
	 * 存放Action和 {@link BroadcastListener} 的映射列表
	 */
	private ArrayList<ActionMap> mActionMaps = new ArrayList<ActionMap>();
	

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
//			Log.i(LOGTAG, "receive broadcast: " + intent.toString());
			String action = intent.getAction();
			for (int i = 0; i < mActionMaps.size(); ) {
				ActionMap map = mActionMaps.get(i);
				if(map.mListenerRef.get() == null){
					mActionMaps.remove(i);//移除无效的监听者
					continue;
				}
				
				if (map.mAction.equals(action)) {
					Log.v(LOGTAG,"src_action="+map.mAction+",dest_action="+action);
//					
					try{
						Log.i(LOGTAG, ""+intent.getStringExtra("resource")+"listener: "+map.mListenerRef.get());
						map.mListenerRef.get().onReceiveAction(context, intent);
					}catch(NullPointerException e){
						mActionMaps.remove(i);//移除无效的监听者
						continue;
					}
				}
				++i;//IMPORTANT!!! don't forget this line
			}
		}
	};

	/**
	 * 用于接收广播消息
	 */
	static public interface BroadcastListener {
		public void onReceiveAction(Context context, Intent intent);
	}

	/**
	 * 用于建立在{@link BroadcastListener} 和 Action之间的对应关系 这关系是一对多的 一个 Listener
	 * 可以监听多个消息
	 */
	private class ActionMap {

		public ActionMap(BroadcastListener l, String action) {
			mListenerRef = new WeakReference<BroadcastListener>(l);
			mAction = action;
		}

		// 指向一个handler
		WeakReference<BroadcastListener> mListenerRef = null;
		String mAction = null;

		@Override
		public boolean equals(Object object) {
			if(object==null) return false;
			if(object instanceof ActionMap) {
				ActionMap o=(ActionMap)object;
				return mAction.equals(o.mAction) && mListenerRef.get() == o.mListenerRef.get();
			}else {
				return false;
			}
		}

	}

	public static BroadcastDispatcher getInstance() {
		if (mDispatcher == null) {
			mDispatcher = new BroadcastDispatcher();
		}
		return mDispatcher;
	}

	public void registerBroadcastListener(String action,
			BroadcastListener listener) {
		ActionMap map = new ActionMap(listener, action);
		IntentFilter filter = new IntentFilter();
		filter.addAction(action);
		mContext.registerReceiver(mReceiver, filter);
		// mActionMaps.add(map);
		addActionMap(map);
	}
	
	public void setup(Context context) {
		mContext = context;
	}
	
	public void setRepeatAlarmNotify(String action,long firstTime,long interval) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(action);
		mContext.registerReceiver(mReceiver, filter);
		Intent intent = new Intent(action);
		PendingIntent sender = PendingIntent.getBroadcast(mContext,
				0, intent, 0);
		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
				interval, sender);
	}
	
	public void setAlarmNotify(String action,long firstTime) {
		Intent intent = new Intent(action);
		setAlarmNotify(intent,firstTime);
	}
	
	public void setAlarmNotify(Intent intent,long firstTime) {
		IntentFilter filter = new IntentFilter();
		mContext.registerReceiver(mReceiver, filter);
		PendingIntent sender = PendingIntent.getBroadcast(mContext,
				0, intent, 0);
		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,  firstTime , sender);
	}
	
	public void sendBroadcast(Intent intent) {
		mContext.sendBroadcast(intent);
	}

	public void registerBroadcastListener(IntentFilter filter,
			BroadcastListener listener) {

		Iterator<String> i = filter.actionsIterator();
		while (i.hasNext()) {
			ActionMap map = new ActionMap(listener, i.next());
			//mActionMaps.add(map);
			addActionMap(map);
		}
		mContext.registerReceiver(mReceiver, filter);
	}

	public void unRegisterListener(BroadcastListener l) {
		for(int i = 0 ; i< mActionMaps.size(); i++) {
			ActionMap map = mActionMaps.get(i);
			if(map.mListenerRef.get() == l) {
				mActionMaps.remove(map);
			}
		}
	}

	/**
	 * 多次添加时，不添加重复数据
	 * @param map
	 */
	private void addActionMap(ActionMap map) {
		if (!mActionMaps.contains(map)) {
			mActionMaps.add(map);
		} else {
			Log.w(LOGTAG, "You add the duplicate data" + map.mAction);
		}
	}
	
	public void cleanBroadcast() {
		mActionMaps.clear();
		mContext.unregisterReceiver(mReceiver);
	}

}

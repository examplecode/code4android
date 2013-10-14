package com.mx.demo.tinycrawler;

import java.util.Observable;
import java.util.Observer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.mx.demo.tinycrawler.TinyCrawler.CrawlStatusNotifyer;

public class CrawlerService extends Service implements CrawlStatusNotifyer,Observer {
	
	
	static final int NOTIFY_CRAWLER_ID = 1;
	
	NotificationManager notificationManager;
	Notification mNotification;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("crawl", " =========== crawl service created ==============");
		
		TinyCrawler.getInstance().setCrawlNotifyer(this);
		
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		Log.i("crawl", " =========== crawl service started ==============");
		
	}
	  
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.i("test", ">>>>>>>>>>>>>>service destoryed<<<<<<<<<<<<<<<<<<");
//		TinyCrawler.getInstance()
		notificationManager.cancel(NOTIFY_CRAWLER_ID);
	}

	@Override
	public void noitfyCrawlProgress(int crawled, int queueSize) {
		String fmtStr = getString(R.string.srv_crawl_progress);
		String statusProgress = String.format(fmtStr, crawled,queueSize);
//		String status = "crawled: [" + crawled + "] /left:[" + queueSize + "]";
	    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, CrawlControlActivity.class), 0);
	    String statContent = getString(R.string.srv_crawl_running);
//		mNotification.setLatestEventInfo( this,statContent,statusProgress,contentIntent);
//		notificationManager.notify(NOTIFY_CRAWLER_ID, mNotification);
		
		showNotify(statContent, statusProgress, contentIntent, false);

	}


	@Override
	public void onCrawlPause() {
	    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, CrawlControlActivity.class), 0);
	    
	    showNotify(R.string.srv_name,R.string.srv_crawl_paused,contentIntent,false);
	}

	@Override
	public void onCrawlResume() {
	    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, CrawlControlActivity.class), 0);
	    showNotify(R.string.srv_name,R.string.srv_crawl_running,contentIntent,false);
//	    showNotify("crawler running","resume to crawl",contentIntent,false);
	}

	@Override
	public void onCrawlStart(String url,String title) {
		if(notificationManager == null){
			notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		notificationManager.cancel(NOTIFY_CRAWLER_ID);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, CrawlControlActivity.class), 0);
		showNotify(R.string.srv_name,R.string.srv_crawl_running,contentIntent,false);
		Log.i("crawl", "notify start");
//		showNotify("cralwl","start crawl....",contentIntent,false);
	}
	
	private void showNotify(String title,String content,PendingIntent penddingIntent,boolean autoCancel) {
		if(notificationManager == null){
			notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		if(mNotification == null) {
			String cont = getString(R.string.srv_crawl_started);
			mNotification = new Notification( R.drawable.ic_crawler, cont,
					System.currentTimeMillis() );
		}
		mNotification.setLatestEventInfo(this, title, content, penddingIntent);
		
		mNotification.flags &= ~Notification.FLAG_ONGOING_EVENT;
		if(autoCancel)
		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		notificationManager.notify(NOTIFY_CRAWLER_ID, mNotification);
	}
	
	private void showNotify(int titleRes,int contRes,PendingIntent penddingIntent,boolean autoCancel) {
//		getString(titleRes);
		showNotify(getString(titleRes),getString(contRes),penddingIntent,autoCancel);
	}

	@Override
	public void onCrawlStop(int error) {
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, TinyCrawlerActivity.class), 0);
		if(error == TinyCrawler.ERR_NONE) {
			showNotify(R.string.srv_name,R.string.srv_crawl_finished,contentIntent,true);
		} else if(error == TinyCrawler.ERR_CANCEL){
			showNotify(R.string.srv_name,R.string.srv_crawl_cancel,contentIntent,false);
		} else {
			showNotify(R.string.srv_name,R.string.srv_crawl_stop_on_error,contentIntent,false);
		}
		Intent i = new Intent(TinyCrawlerActivity.ACTION_CRAWL_FINISHED);
		sendBroadcast(i);
	}

	@Override
	public void update(Observable paramObservable, Object paramObject) {
		CrawlSettings settings = (CrawlSettings) paramObservable;
		TinyCrawler.mBaseDir = settings.mCrawlDataDir;
		TinyCrawler.getInstance().mDownloadImage = settings.mDownloadImage;
		
		
//		TinyCrawler.getInstance().
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
		System.gc();
		
	}

}

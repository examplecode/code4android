package com.mx.demo.tinycrawler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class CrawlerRequestReceiver extends BroadcastReceiver {
	
	public static boolean sServiceStartd = false;
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals("com.mx.intent.action.PLUGIN")) {
			try {
				String url = intent.getStringExtra("url");
				String title = intent.getStringExtra("title");
				Log.i("crawl", "receiver intent start crawler,url="+url);
				int defautlLevel = CrawlSettings.getInstance().mCrawlLevel;
				if(TextUtils.isEmpty(url) || url.startsWith("mx") || url.startsWith("javascript")){
					Toast.makeText(context, R.string.toast_unsupported_url, Toast.LENGTH_LONG).show();
					return;
				}
//				if(TextUtils.isEmpty(title)){
//					title = context.getString(R.string.toast_untitled);
//				}
				ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkinfo = manager.getActiveNetworkInfo();
				if(CrawlSettings.getInstance().mJustCrawlInWifiType && networkinfo.getType() != ConnectivityManager.TYPE_WIFI) {
					Toast.makeText(context, R.string.toast_crawl_in_wifi, Toast.LENGTH_LONG).show();
					return ;
				}
				if(!CrawlerJsObj.mDoCleaningData) {
					TinyCrawler.getInstance().crawl(url,defautlLevel,title);
				} else {
					Toast.makeText(context, R.string.toast_now_is_cleaning_data, Toast.LENGTH_LONG).show();
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		} else if(intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE") && TinyCrawler.getInstance().getStatues() != TinyCrawler.STATUS_FINISH) {
			
			int status = TinyCrawler.getInstance().getStatues();
			if(CrawlSettings.getInstance().mJustCrawlInWifiType) {
				ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkinfo = manager.getActiveNetworkInfo();
				if(networkinfo != null) {
					if( networkinfo.getType() != ConnectivityManager.TYPE_WIFI ) {
						if( status == TinyCrawler.STATUS_RUNNING) {
							Toast.makeText(context, R.string.toast_pause_crawl_non_wifi, Toast.LENGTH_LONG).show();
							TinyCrawler.getInstance().pause();
						}
			
					} else {
						
						if(status == TinyCrawler.STATUS_PAUSE) {
							Toast.makeText(context, R.string.toast_resume_crawl_in_wifi, Toast.LENGTH_LONG).show();
							TinyCrawler.getInstance().resume();
						}
		
					}
				}
				
			}
			

		}
		

	}

}

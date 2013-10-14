package com.mx.demo.tinycrawler;

import java.util.Observable;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;


public class CrawlSettings extends Observable{

	public final static String PREF_CRAWL_LEVEL = "list_crawl_level_setting";
	public final static String PREF_CRAWL_IN_WIFI = "checkbox_crawl_in_wifi_env";
	public final static String PREF_CRAWL_DOWNLOAD_IMAGE = "checkbox_download_image";
	public final static String PREF_CRAWL_DATA_DIR = "edit_crawl_data_dir_setting";
	
	
	private static CrawlSettings sSingleton;
	
	
	public boolean mJustCrawlInWifiType = true;
	public String mCrawlDataDir = null;
	public int mCrawlLevel = 1;
	public boolean mDownloadImage = true;
	
	private Context mContext = null;
	
	public static CrawlSettings getInstance() {
		if(sSingleton == null) {
			sSingleton = new CrawlSettings();
		}
		return sSingleton;
	}
	
	private CrawlSettings() {
		mCrawlDataDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/data";
	}

	public void loadSetings(Context ctx) {
		mContext = ctx;
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		syncSharedPreferences(p);
	}
	void syncSharedPreferences(SharedPreferences p) {
		mJustCrawlInWifiType=p.getBoolean(PREF_CRAWL_IN_WIFI, mJustCrawlInWifiType);
		mCrawlLevel =Integer.parseInt(p.getString(PREF_CRAWL_LEVEL, "1"));
		mDownloadImage = p.getBoolean(PREF_CRAWL_DOWNLOAD_IMAGE, mDownloadImage);
		String dir =  p.getString(PREF_CRAWL_DATA_DIR, mCrawlDataDir);
		if(dir != null && !dir.equals("")) {
			mCrawlDataDir = dir;
		}
		update();
	}
	
	/**
	 * 通知各个observers,数据发生改变
	 */
	void update() {
		setChanged();
		notifyObservers();
	}
	public void setPreferences(String key,String value){
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(key, value).commit();
	}
	
}

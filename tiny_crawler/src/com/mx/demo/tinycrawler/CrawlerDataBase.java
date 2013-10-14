package com.mx.demo.tinycrawler;



import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CrawlerDataBase  {

	
	private static final String SQL_CRAWL_HIS = "CREATE TABLE IF NOT EXISTS " + "crawl_history" + " (" 
			+ "id"			+ " INTEGER PRIMARY KEY," 
			+ "url"		+ " TEXT,"
			+ "title"			+ " TEXT," 
			+ ");";
	
	private static final String SQL_CRAWL_SCHEDULE = "CREATE TABLE IF NOT EXISTS " + "crawl_schedule" + " (" 
			+ "id"			+ " INTEGER PRIMARY KEY," 
			+ "url"		+ " TEXT,"
			+ "title"		+ " TEXT,"
			+ "schedule"			+ " TEXT," 
			+ ");";
	
	private static CrawlerDataBase sInstance = null;
	
	SQLiteOpenHelper mDbHellper = null;
	Context mContext = null;
	
	private CrawlerDataBase() {
	}
	
	public static CrawlerDataBase getInstance() {
		if(sInstance == null) {
			sInstance = new CrawlerDataBase();
		}
		return sInstance;
	}
	
	public void init(Context context) {
		mContext = context;
		mDbHellper =  new SQLiteOpenHelper(context, "crawler.db", null, 1) {
			
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				
			}
			
			@Override
			public void onCreate(SQLiteDatabase db) {
					db.execSQL(SQL_CRAWL_HIS);
					db.execSQL(SQL_CRAWL_SCHEDULE);
			}
		};
	}
	
	public void  logCrawlHistory(String url,String title) {
		
	}
	
	public void addSchedule(String url,String title,String schedule) {
		
	}
	
	public void removeSchedule(String url){
		
	}

}

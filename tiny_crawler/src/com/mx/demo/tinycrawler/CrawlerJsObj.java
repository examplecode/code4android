package com.mx.demo.tinycrawler;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


public class CrawlerJsObj {
	
	ArrayList<String> mHistoryList = new ArrayList<String>();
	Context mContext = null;
	
	/**
	 * 标识当前正在清理抓取数据
	 */
	public static boolean mDoCleaningData = false;
	
    private  class ClearOfflineData extends AsyncTask<String,Void,Void> {

		@Override
		protected Void doInBackground(String... params) {
			
			deleteDir(params[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Toast.makeText(mContext, R.string.toast_offline_data_has_clean, Toast.LENGTH_LONG).show();
			mHistoryList.clear();
			mDoCleaningData = false;
		}
	    public  void deleteDir(String path)
	    {
	    	File file = new File(path);
	    	if (file.exists())
	    	{
	    		if (file.isDirectory())
	    		{
	    			File[] files = file.listFiles();
	    			for (File subFile : files)
	    			{
	    				if (subFile.isDirectory())
	    					deleteDir(subFile.getPath());
	    				else
	    					subFile.delete();
	    			}
	    		}
	    		file.delete();
	    	}
	    }
    }
	
	public CrawlerJsObj(Context cxt) {
		mContext = cxt;
//		readHistoryToArray();
	}
	
	
	public int historyCount() {
		Log.i("crawl", "read history");
		if(mDoCleaningData){
			Toast.makeText(mContext, R.string.toast_clean_data_in_bg, Toast.LENGTH_LONG).show();
			return -1;
		}
		return mHistoryList.size();
	}
	
	public void cleanHistory() {
//		TinyCrawler.getInstance().resetHistoryLog();
		
		Toast.makeText(mContext, R.string.toast_clean_data_in_bg, Toast.LENGTH_LONG).show();
		if(!mDoCleaningData) {
			new ClearOfflineData().execute(TinyCrawler.getInstance().mBaseDir);
			mDoCleaningData = true;
		}
	}
	
	public void openSetting() {
		Intent i = new Intent();
		i.setClass(mContext, CrawlPreferenceActivity.class);
		mContext.startActivity(i);
	}
	
	public String getHistoryItem(int index) {
		JSONObject jobj = new JSONObject();
		String item = mHistoryList.get(index);
		String[] array = item.split("\t");
		try {
			if(array.length < 2)  {
				jobj.put("title", "noname");
				jobj.put("url", array[0]);
			} else {
				jobj.put("title", array[0]);
				jobj.put("url", array[1]);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return jobj.toString();
//		return "{ \"url\": \"http://3g.sina.cn\", \"title\":\"新闻\"} ";

	}
}

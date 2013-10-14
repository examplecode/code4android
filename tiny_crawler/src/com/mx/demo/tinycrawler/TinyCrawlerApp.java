package com.mx.demo.tinycrawler;


import android.app.Application;
import android.content.Intent;
public class TinyCrawlerApp extends Application {
	
    
    public void onCreate() {
    	
    	
    	CrawlSettings.getInstance().loadSetings(this);
    	TinyCrawler.mBaseDir = CrawlSettings.getInstance().mCrawlDataDir;
    	TinyCrawler.getInstance().mDownloadImage = CrawlSettings.getInstance().mDownloadImage;

//      VMRuntime.getRuntime().setMinimumHeapSize(HEAP_SIZE); //调整堆内存
         
      //启动抓取线程
       startService(new Intent(this,CrawlerService.class));
        
    }
    
        
}

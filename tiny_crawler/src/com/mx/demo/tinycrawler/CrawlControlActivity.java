package com.mx.demo.tinycrawler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class CrawlControlActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.crawl_control_panel);
		
		
		final Button btnControl = (Button )findViewById(R.id.btn_control);
		Button btnStop = (Button )findViewById(R.id.btn_stop);
		btnStop.setText(R.string.ctr_stop);
		
		btnControl.setText(R.string.ctr_pause);
		int status = TinyCrawler.getInstance().getStatues();
		if(status == TinyCrawler.STATUS_RUNNING) {
			btnControl.setText(R.string.ctr_pause);
			
		} else if(status == TinyCrawler.STATUS_PAUSE){
			btnControl.setText(R.string.ctr_resume);
		}  else if(status == TinyCrawler.STATUS_FINISH ){
			btnControl.setEnabled(false);
			btnStop.setEnabled(false);
			Toast.makeText(CrawlControlActivity.this, R.string.toast_crawl_has_finished, Toast.LENGTH_LONG).show();
		}
		btnControl.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(TinyCrawler.getInstance().getStatues() == TinyCrawler.STATUS_RUNNING) {
					Toast.makeText(CrawlControlActivity.this, R.string.toast_pause_crawl, Toast.LENGTH_LONG).show();
					TinyCrawler.getInstance().pause();
//					btnControl.setImageResource(R.drawable.crawl_btn_start);
					btnControl.setText(R.string.ctr_resume);
				} else if(TinyCrawler.getInstance().getStatues() == TinyCrawler.STATUS_PAUSE) {
					Toast.makeText(CrawlControlActivity.this, R.string.toast_resume_crawl, Toast.LENGTH_LONG).show();
					TinyCrawler.getInstance().resume();
//					btnControl.setImageResource(R.drawable.crawl_btn_pause);
					btnControl.setText(R.string.ctr_pause);
				} else if(TinyCrawler.getInstance().getStatues() == TinyCrawler.STATUS_FINISH ) {
//					Toast.makeText(CrawlControlActivity.this, R.string.srv_crawl_finished, Toast.LENGTH_LONG).show();
				}
			}
		});
		
	
//		btnStop.setImageResource(R.drawable.crawl_btn_stop);
		
		
		btnStop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				TinyCrawler.getInstance().cancel();
				Toast.makeText(CrawlControlActivity.this, R.string.toast_stop_crawl, Toast.LENGTH_LONG).show();
				finish();
			}
		});
		
		Button btnSeting = (Button )findViewById(R.id.btn_seting);
//		btnSeting.setImageResource(R.drawable.crawl_btn_setting);
		btnSeting.setText(R.string.ctr_setting);
		btnSeting.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 Intent intent = new Intent(CrawlControlActivity.this,CrawlPreferenceActivity.class);
				 CrawlControlActivity.this.startActivity(intent);
			}
		});
		
		WindowManager m = getWindowManager();  
		Display d = m.getDefaultDisplay();  //为获取屏幕宽、高  
		  
		LayoutParams p = getWindow().getAttributes();  //获取对话框当前的参数值  
		p.height = (int) (d.getHeight() * 0.28);   //高度设置为屏幕的0.6  
		p.width = (int) (d.getWidth() * 0.96);    //宽度设置为屏幕的0.95  
		  
		getWindow().setAttributes(p);     //设置生效  
		
	}

}

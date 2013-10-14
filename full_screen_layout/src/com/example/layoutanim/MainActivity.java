package com.example.layoutanim;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.MeasureSpec;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	Handler handler = new Handler();

	View top_cont = null;
	View bootom_cont = null;
	WebView wv = null;
	FrameLayout mainContent = null;
	int layout_scale = 5;

	int top_h = 0;
	int bootom_h = 0;

	boolean fullScreen = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// ===== top ==========//

		top_cont = findViewById(R.id.top_content);

		bootom_cont = findViewById(R.id.bottom_content);

		top_h = top_cont.getHeight();
		bootom_h = bootom_cont.getHeight();

		// ====== main content =========//
		mainContent = (FrameLayout) findViewById(R.id.main_content);

		// ImageView img = new ImageView(this);
		// img.setImageResource(R.drawable.guide03);
		// mainContent.addView(img);
		wv = new WebView(this);
		wv.loadUrl("http://3g.sina.com.cn");
		mainContent.addView(wv);
		
		top_cont.setClickable(true);
		top_cont.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "start full screen",
						Toast.LENGTH_LONG).show();
//				startAnimation();
				fullScreen = true;
				// mainContent.layout(l, t, r, b);
			}
		});
		
		bootom_cont.setClickable(true);
		bootom_cont.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
	
				Toast.makeText(MainActivity.this, "layout", Toast.LENGTH_LONG).show();
//				startAnimation();
			
				
			}
		});
		
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				top_h = top_cont.getHeight();
				bootom_h = bootom_cont.getHeight();
				mainContent.layout(mainContent.getLeft(), mainContent.getTop() + top_h
						, mainContent.getRight(),
						mainContent.getBottom() );
				
			}
		});
		
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	void startAnimation() {
	
		Runnable layoutRunable = new Runnable() {
			
			public void run() {

				int top_move = top_h > layout_scale ? layout_scale : top_h;
				if (top_h > 0) {
					top_cont.layout(top_cont.getLeft(), top_cont.getTop()
							- top_move, top_cont.getRight(),
							top_cont.getBottom() - top_move);
					top_h -= layout_scale;

				}

				int bootom_move = bootom_h > layout_scale ? layout_scale
						: bootom_h;

				if (bootom_h > 0) {
					bootom_cont.layout(bootom_cont.getLeft(),
							bootom_cont.getTop() + bootom_move,
							bootom_cont.getRight(), bootom_cont.getBottom()
									+ bootom_move);
					bootom_h -= layout_scale;
				}

				mainContent.layout(mainContent.getLeft(), mainContent.getTop()
						- top_move, mainContent.getRight(),
						mainContent.getBottom() + bootom_move);

				if (top_h > 0 || bootom_h > 0) {
					handler.postDelayed(this, 15);
				}
				layout_scale += 3;

			}
		};

		handler.post(layoutRunable);
	}

}

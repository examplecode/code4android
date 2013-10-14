package com.examplecode.mutilui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class PadBrowserActivtyDelegate implements ActivityDelegate {

	private Activity mActivity = null;
	
	public PadBrowserActivtyDelegate(Activity activity) {
		mActivity = activity;
	}
	
	@Override
	public View getContentView() {
		// TODO Auto-generated method stub
		return  View.inflate(mActivity, R.layout.main_pad, null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		
	}

}

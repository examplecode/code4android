package com.examplecode.mutilui;

import android.app.Activity;
import android.os.Bundle;

public class MutilUiDemoActivity extends Activity {
	
	public final int MOBILE_DEVICE = 0;
	
	public final int PAD_DEVICE = 1;
	
	private int deviceType  = PAD_DEVICE;
	
	
	private  ActivityDelegate mActivityDelegate = null;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initActivityDelegate();
        mActivityDelegate.onCreate(savedInstanceState);
        
        setContentView(mActivityDelegate.getContentView());
    }
    
    
    void initActivityDelegate() {
        if(deviceType == MOBILE_DEVICE) {
        	mActivityDelegate = new MobileBrowserActivtyDelegate(this);
        } else if (deviceType == PAD_DEVICE) {
        	mActivityDelegate = new PadBrowserActivtyDelegate(this);
        }
    }
}
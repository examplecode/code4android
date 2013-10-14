package com.examplecode.mutilui;


import android.os.Bundle;
import android.view.View;

public interface ActivityDelegate  {
	
	public View getContentView();

	public void onCreate(Bundle savedInstanceState);
	
	public void onStart();
	
	public void onResume();
	
	public void onPause();
	
}

package com.example.layoutanim;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MyFrameLayout extends FrameLayout {
	
	FrameLayout top_frame = null;
	FrameLayout bootom_frame = null;
	FrameLayout content_frame = null;
	
	private int mLastMotionX, mLastMotionY;
	 
	boolean full_screen = false;
    // 移动的阈值
    private static final int TOUCH_SLOP = 20;
    
    // 是否移动了
    private boolean isMoved;

	public MyFrameLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public MyFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MyFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		
//		View content = findViewById(R.id.main_content);
		for(int i= 0; i < getChildCount(); i++) {
			View c = getChildAt(i);
			if(c.getId() == R.id.top_content) {
				top_frame = (FrameLayout) c;
			} else if(c.getId() == R.id.main_content) {
				content_frame = (FrameLayout) c;
			} else if(c.getId() == R.id.bottom_content) {
				bootom_frame =  (FrameLayout) c;
			}
		}
		content_frame.layout(content_frame.getLeft(), content_frame.getTop() + top_frame.getHeight(), content_frame.getRight(), content_frame.getBottom() +  top_frame.getHeight());
		
	}
	
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        
        switch (ev.getAction()) {
        	case MotionEvent.ACTION_UP:
        		break;
        	case MotionEvent.ACTION_DOWN:
        	{
                mLastMotionX = x;
                mLastMotionY = y;
                isMoved = false;
                break;
        	}
        	case MotionEvent.ACTION_MOVE:
        	{
        		 if (isMoved)  break;

                 if (Math.abs(mLastMotionX - x) > TOUCH_SLOP
                         || Math.abs(mLastMotionY - y) > TOUCH_SLOP) {
                     // 移动超过阈值，则表示移动了
                     isMoved = true;
                }
               if (isMoved && (y > mLastMotionY)) {  // pull down
            	   if ( Math.abs(y - mLastMotionY) > Math.abs(x- mLastMotionX) && full_screen ) // 夹角大于45度。
	               {
            		   pullDown();
	               }
              
               } else if(isMoved && (y < mLastMotionY) && !full_screen) {  // push up
            	   if ( Math.abs(y - mLastMotionY) > Math.abs(x- mLastMotionX))  {  // 夹角大于45度。
    	               
            		   pushUp();
	               }
               }
                 
                 
               
                 break;
        	}
               
        		
        		
        }
        	
		// TODO Auto-generated method stub
		return super.dispatchTouchEvent(ev);
	}
	
	
	private void pullDown() {
		if(top_frame == null || content_frame == null || bootom_frame == null) return;
		
		Toast.makeText(getContext(), "do pull down", Toast.LENGTH_LONG).show();
	
		top_frame.layout(top_frame.getLeft(), top_frame.getTop() + top_frame.getHeight() , top_frame.getRight(), top_frame.getBottom() + top_frame.getHeight());
		content_frame.layout(content_frame.getLeft(), content_frame.getTop() + top_frame.getHeight() , content_frame.getRight(), content_frame.getBottom() + top_frame.getHeight());
		bootom_frame.layout(bootom_frame.getLeft(), bootom_frame.getTop() - bootom_frame.getHeight() , bootom_frame.getRight(), bootom_frame.getBottom() - bootom_frame.getHeight());
		
		full_screen = false;
	}
	
	private void pushUp() {
		if(top_frame == null || content_frame == null || bootom_frame == null) return;
		
		Toast.makeText(getContext(), "do push up", Toast.LENGTH_LONG).show();
		
		top_frame.layout(top_frame.getLeft(), top_frame.getTop() - top_frame.getHeight() , top_frame.getRight(), top_frame.getBottom() - top_frame.getHeight());
		content_frame.layout(content_frame.getLeft(), content_frame.getTop() - top_frame.getHeight() , content_frame.getRight(), content_frame.getBottom() - top_frame.getHeight());
		bootom_frame.layout(bootom_frame.getLeft(), bootom_frame.getTop() + bootom_frame.getHeight() , bootom_frame.getRight(), bootom_frame.getBottom() + bootom_frame.getHeight());
		
		full_screen = true;
		
	}


}

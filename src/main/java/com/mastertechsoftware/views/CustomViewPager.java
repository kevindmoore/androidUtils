package com.mastertechsoftware.views;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Custom ViewPager that allows you to turn on/off swiping
 */
public class CustomViewPager extends ViewPager {

	protected boolean enableSwiping = true;
    protected boolean isScrolling = false;

    /**
     * Distance a touch event can move before we try to intercept it
     */
    protected int mTouchSlop;

	public CustomViewPager(Context context) {
		super(context);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
	}

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
	}

    @Override
	public boolean onTouchEvent(MotionEvent event) {
		if (this.enableSwiping) {  //if swiping is enabled, handled everything normally
			return super.onTouchEvent(event);
		}

        int action = event.getAction();

        //handle the case of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the scroll. and register as handled
            isScrolling = false;
            return true;
        }

        if ((action & MotionEventCompat.ACTION_MASK) == MotionEvent.ACTION_MOVE) {
            return true;  //register the event as handled, and ignore
        }

		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (this.enableSwiping) { //if swiping is enabled, handle everything normally
			return super.onInterceptTouchEvent(event);
		}


        //otherwise, deal with our special cases
        int action = event.getAction();

        //handle the case of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the scroll.
            isScrolling = false;
            return false; // Do not intercept touch event, let the child handle it
        }

        if(action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_SCROLL) {
            if(isScrolling) {
                //we've already decided we're scrolling, so just return true
                return true;
            }

            //otherwise, decide if we're trying to scroll
            if(mTouchSlop < getDistanceX(event)) {
                //if we've moved farther in the x direction than the touch slop (which differentiates between a touch event and a scroll)
                isScrolling = true;
                return true;
            }

        }

        //otherwise, we don't care, return false and let the child handle it
		return false;
	}

	public boolean isEnableSwiping() {
		return enableSwiping;
	}

	public void setEnableSwiping(boolean enableSwiping) {
		this.enableSwiping = enableSwiping;
	}

    protected int getDistanceX(MotionEvent event) {
        int historySize = event.getHistorySize();
        if(historySize < 1) {
            return 0; //first point, no movement
        }

        float startX = event.getHistoricalX(0);
        float currentX = event.getX();
        return Math.abs(Math.round(currentX - startX));
    }

    protected int getDistanceY(MotionEvent event) {
        int historySize = event.getHistorySize();
        if(historySize < 1) {
            return 0; //first point, no movement
        }

        float startY = event.getHistoricalY(0);
        float currentY = event.getY();
        return Math.abs(Math.round(currentY - startY));
    }
}

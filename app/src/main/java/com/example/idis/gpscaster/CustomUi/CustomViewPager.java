package com.example.idis.gpscaster.CustomUi;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by IDIS on 2016-11-29.
 */

public class CustomViewPager extends ViewPager {
    static final String TAG = "Motion";

    // boolean value to decide swiping disabled
    private boolean swipingEnabled;

    //constructor
    public CustomViewPager(Context context) {
        super(context);
        this.swipingEnabled = true;
    }
    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.swipingEnabled = true;
    }

    // Don't move with finger swiping
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (this.swipingEnabled) {
            return super.onTouchEvent(ev);
        }

        return false;
    }

    // Don't Intercept with finger swiping
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(this.swipingEnabled) {
            return super.onInterceptTouchEvent(ev);
        }

        return false;
    }


    // I can set enable value with this method -> false : don't move
    public void setPagingEnabled(boolean swipingEnabled) {
        this.swipingEnabled = swipingEnabled;
    }

}

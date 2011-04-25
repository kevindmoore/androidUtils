package com.mastertechsoftware.buton;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

import java.util.Timer;
import java.util.TimerTask;

/**
 * User: Kevin
 * Date: Mar 31, 2010
 */
public class CustomButton extends ImageButton {
    private Drawable defaultImage;
    private Drawable downImage;
    private Drawable upImage;
    private Drawable disabledImage;
    private static final int SHOW_IMAGE_TIME = 1000;

    public CustomButton(Context context) {
        super(context);
        init();
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init() {
        setPadding(0,0,0,0);
        setBackgroundDrawable(null);
    }
    public Drawable getDefaultImage() {
        return defaultImage;
    }

    public void setDefaultImage(Drawable defaultImage) {
        this.defaultImage = defaultImage;
        setImageDrawable(defaultImage);
    }

    public Drawable getDownImage() {
        return downImage;
    }

    public void setDownImage(Drawable downImage) {
        this.downImage = downImage;
    }

    public Drawable getUpImage() {
        return upImage;
    }

    public void setUpImage(Drawable upImage) {
        this.upImage = upImage;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        if ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_MOVE)) {
            if (downImage != null && isEnabled()) {
                setImageDrawable(downImage);
            }
        } else if (action == MotionEvent.ACTION_UP && isEnabled()) {
            if (upImage != null) {
                setImageDrawable(upImage);
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (defaultImage != null) {
                            setImageDrawable(defaultImage);
                        }
                    }
                }, 0, SHOW_IMAGE_TIME);
            } else  {
                if (defaultImage != null) {
                    setImageDrawable(defaultImage);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public void setDisabledButton(BitmapDrawable drawable) {
        this.disabledImage = drawable;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            setImageDrawable(defaultImage);
        } else if (disabledImage != null) {
            setImageDrawable(disabledImage);
        }
    }
}


package com.mastertechsoftware.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.mastertechsoftware.util.log.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DragableListView extends ListView implements DraggableInterface {
    protected DragableHelper dragableHelper;

    public DragableListView(Context context) {
        super(context);

        init();
    }

    public DragableListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public DragableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        dragableHelper = new DragableHelper(getContext(), this, ListView.class, this);
    }

    public void setDragableIconWidth(int dragableIconWidth) {
        dragableHelper.setDragableIconWidth(dragableIconWidth);
    }

    public void setDragableListListener(DragableListListener l) {
        dragableHelper.setDragableListListener(l);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (dragableHelper.onInterceptTouchEvent(ev)) {
            Logger.debug(this, "DragableListView:onInterceptTouchEvent. Helper returned true");
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }



    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return super.onTouchEvent(ev);
        }
        if (dragableHelper.onTouchEvent(ev)) {
            Logger.debug(this, "DragableListView:onTouchEvent. Helper returned true");
            return true;
        }
        return super.onTouchEvent(ev);
    }

}

package com.mastertechsoftware.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;
import android.widget.ListView;
/**
 * Override GridView to make it draggable.
 */
public class DraggableGridView extends GridView implements DraggableInterface {
    protected DragableHelper dragableHelper;

    public DraggableGridView(Context context) {
        super(context);
        init();
    }

    public DraggableGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DraggableGridView(Context context, AttributeSet attrs, int defStyle) {
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
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }



    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (dragableHelper.onTouchEvent(ev)) {
            return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public int getHeaderViewsCount() {
        return 0;
    }

    @Override
    public void setSelectionFromTop(int position, int y) {
    }
}


package com.mastertechsoftware.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.mastertechsoftware.util.log.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DragableHelper {
    protected Method mSmoothScrollToPosition = null;
    protected Method mSmoothScrollBy = null;

    public static final int OVER_SCROLL_ALWAYS = 0;
    public static final int OVER_SCROLL_IF_CONTENT_SCROLLS = 1;
    public static final int OVER_SCROLL_NEVER = 2;

    protected Method mSetOverScrollMethod = null;

    // Maximum number of rows we'll smooth scroll to.
    protected final static int MAX_SMOOTH_SCROLL_DISTANCE = 20;

    public static final int DRAGABLE_ICON_WIDTH = 86;
    private boolean dragging = false;
    private boolean debugging = true;

    private enum REMOVE_MODE {
        NONE,
        FLING,
        SLIDE,
        TRASH
    }
    protected ImageView mDragView;
    protected WindowManager mWindowManager;
    protected WindowManager.LayoutParams mWindowParams;
    /**
     * At which position is the item currently being dragged. Note that this
     * takes in to account header items.
     */
    protected int mDragPos;
    /**
     * At which position was the item being dragged originally
     */
    protected int mSrcDragPos;
    protected int mDragPointX; // at what x offset inside the item did the user
                             // grab
                             // it
    protected int mDragPointY; // at what y offset inside the item did the user
                             // grab
                             // it
    protected int mXOffset; // the difference between screen coordinates and
                          // coordinates in this view
    protected int mYOffset; // the difference between screen coordinates and
                          // coordinates in this view
    protected DragableListListener mDragableListListener;
    protected int mUpperBound;
    protected int mLowerBound;
    protected int mHeight;
    protected GestureDetector mGestureDetector;
    protected REMOVE_MODE mRemoveMode = REMOVE_MODE.NONE;
    protected Rect mTempRect = new Rect();
    protected int mTouchSlop;
    protected int mItemHeightNormal;
    protected int mItemHeightHalf;
    protected Drawable mTrashcan;
    protected int dragableIconWidth = DRAGABLE_ICON_WIDTH;

    protected final static int DRAG_VIEW_OPACITY = 229;
    protected Context context;
    protected AbsListView listView;
    protected DraggableInterface draggableInterface;
    protected Class listClass;

    public DragableHelper(Context context, AbsListView listView, Class listClass, DraggableInterface draggableInterface) {
        this.context = context;
        this.listView = listView;
        this.listClass = listClass;
        this.draggableInterface = draggableInterface;
        init();
    }


    protected void init() {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mItemHeightNormal = -1;
        mItemHeightHalf = -1;
        initMethods();
    }

    public void setDragableIconWidth(int dragableIconWidth) {
        this.dragableIconWidth = dragableIconWidth;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (debugging) {
            Logger.debug(this, "onInterceptTouchEvent");
        }
        if (mDragableListListener != null && mGestureDetector == null) {
            if (mRemoveMode == REMOVE_MODE.FLING) {
                mGestureDetector = new GestureDetector(context,
                        new SimpleOnGestureListener() {
                            @Override
                            public boolean onFling(MotionEvent e1, MotionEvent e2,
                                    float velocityX, float velocityY) {
                                if (mDragView != null) {
                                    if (velocityX > 1000) {
                                        Rect r = mTempRect;
                                        mDragView.getDrawingRect(r);
                                        if (e2.getX() > r.right * 2 / 3) {
                                            // fast fling right with release
                                            // near the right edge of
                                            // the screen
                                            stopDragging();
                                            mDragableListListener.remove(mSrcDragPos);
                                            unExpandViews(true);
                                        }
                                    }
                                    // flinging while dragging should have no
                                    // effect
                                    return true;
                                }
                                return false;
                            }
                        });
            }
        }

        if (mDragableListListener != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (debugging) {
                        Logger.debug(this, "onInterceptTouchEvent:ACTION_DOWN");
                    }
                    dragging = false;
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();

                    // Make sure we're not trying to re-order an invalid item
                    // or a header view.
                    int itemnum = listView.pointToPosition(x, y);
                    if (debugging) {
                        Logger.debug(this, "ACTION_DOWN: x = " + x + " y =  " + y + " itemnum = " + itemnum);
                    }
                    if (itemnum == AdapterView.INVALID_POSITION ||
                            itemnum < draggableInterface.getHeaderViewsCount()) {
                        break;
                    }

                    ViewGroup item =
                            (ViewGroup) listView.getChildAt(
                                    itemnum - listView.getFirstVisiblePosition());

                    // Initialize item heights
                    if (mItemHeightNormal == -1) {
                        mItemHeightNormal = item.getHeight();
                        mItemHeightHalf = mItemHeightNormal / 2;
                    }

                    mDragPointX = x - item.getLeft();
                    mDragPointY = y - item.getTop();
                    mXOffset = ((int) ev.getRawX()) - x;
                    mYOffset = ((int) ev.getRawY()) - y;
                    // The left side of the item is the grabber for dragging the
                    // item
                    if (x < dragableIconWidth) {
                        if (debugging) {
                            Logger.debug(this, "onInterceptTouchEvent:ACTION_DOWN. X less than draggable width");
                        }

                        Bitmap bitmap = mDragableListListener.startDragging(x, y, item);
                        if (bitmap != null) {
                            startDragging(bitmap, x, y);
                        }
                        dragging = true;
                        mDragPos = itemnum;
                        mSrcDragPos = mDragPos;
                        mHeight = listView.getHeight();
                        int touchSlop = mTouchSlop;
                        mUpperBound = Math.min(y - touchSlop, mHeight / 3);
                        mLowerBound = Math.max(y + touchSlop, mHeight * 2 / 3);
                        return false;
                    } else {
                        dragging = false;
                    }
                    stopDragging();
                    break;
            }
        }
        return false;
    }

    /*
     * pointToPosition() doesn't consider invisible views, but we need to, so
     * implement a slightly different version.
     */
    protected int myPointToPosition(int x, int y) {

        if (y < 0) {
            // when dragging off the top of the screen, calculate position
            // by going back from a visible item
            int pos = myPointToPosition(x, y + mItemHeightNormal);
            if (pos > 0) {
                return pos - 1;
            }
        }

        Rect frame = mTempRect;
        final int count = listView.getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = listView.getChildAt(i);
            child.getHitRect(frame);
            if (frame.contains(x, y)) {
                return listView.getFirstVisiblePosition() + i;
            }
        }
        return listView.INVALID_POSITION;
    }


    protected void adjustScrollBounds(int y) {
        if (y >= mHeight / 3) {
            mUpperBound = mHeight / 3;
        }
        if (y <= mHeight * 2 / 3) {
            mLowerBound = mHeight * 2 / 3;
        }
    }


    public boolean onTouchEvent(MotionEvent ev) {
        if (mGestureDetector != null) {
            mGestureDetector.onTouchEvent(ev);
        }
        if (!dragging) {
            return false;
        }
        if (debugging) {
            Logger.debug(this, "onTouchEvent");
        }
        if ((mDragableListListener != null) && mDragView != null) {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (debugging) {
                        Logger.debug(this, "onTouchEvent:ACTION_UP or ACTION_CANCEL");
                    }
                    Rect r = mTempRect;
                    mDragView.getDrawingRect(r);
                    stopDragging();
                    if (action == MotionEvent.ACTION_CANCEL) {
                        break;
                    }
                    if (mRemoveMode == REMOVE_MODE.SLIDE && ev.getX() > r.right * 3 / 4) {
                        mDragableListListener.remove(mSrcDragPos);
                        unExpandViews(true);
                    } else {
                        int numheaders = draggableInterface.getHeaderViewsCount();
                        int x = (int) ev.getX();
                        int y = (int) ev.getY();
                        mDragPos = listView.pointToPosition(x, y);
                        if (debugging) {
                            Logger.debug(this, "Final Drag Position is " + mDragPos);
                        }
                        mDragPos = (mDragPos >= numheaders ? mDragPos : numheaders);
                        mDragPos = (mDragPos < listView.getCount() ? mDragPos : listView.getCount() - 1);
                        if (debugging) {
                            Logger.debug(this, "Final Drag Position is " + mDragPos);
                        }
                        mDragableListListener.drop(mSrcDragPos - numheaders,
                                mDragPos - numheaders);
                        unExpandViews(false);
                    }
                    break;

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    if (debugging) {
                        Logger.debug(this, "onTouchEvent:ACTION_DOWN or ACTION_MOVE");
                    }
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    dragView(x, y);
//                    int itemnum = getItemForPosition(y);
                    int itemnum = listView.pointToPosition(x, y);
                    if (debugging) {
                        Logger.debug(this, "ACTION_MOVE: x= " + x + " y= " + y + " itemnum= " + itemnum);
                    }
                    if (itemnum >= 0) {
                        if (action == MotionEvent.ACTION_DOWN || itemnum != mDragPos) {
                            int numheaders = draggableInterface.getHeaderViewsCount();
                            if (mDragableListListener != null &&
                                    mDragPos >= numheaders &&
                                    mDragPos < listView.getCount()) {
                                mDragableListListener.drag(mDragPos - numheaders,
                                        itemnum - numheaders);
                            }
                            mDragPos = itemnum;
                            if (debugging) {
                                Logger.debug(this, "Setting Drag Position to " + mDragPos);
                            }
                        }
                        int speed = 0;
                        adjustScrollBounds(y);
                        if (debugging) {
                            Logger.debug(this, "Y: " + y + " mUpperBound " + mUpperBound + " mLowerBound " + mLowerBound);
                        }
                        if (y > mLowerBound) {
                            // scroll the list up a bit
                            if (listView.getLastVisiblePosition() < listView.getCount() - 1) {
                                speed = y > (mHeight + mLowerBound) / 2 ? 16 : 4;
                            } else {
                                speed = 0;
                            }
                        } else if (y < mUpperBound) {
                            // scroll the list down a bit
                            speed = y < mUpperBound / 2 ? -16 : -4;
                            if (listView.getFirstVisiblePosition() == 0
                                    && listView.getChildAt(0).getTop() >= listView.getPaddingTop()) {
                                // if we're already at the top, don't try to
                                // scroll, because
                                // it causes the framework to do some extra
                                // drawing that messes
                                // up our animation
                                speed = 0;
                            }
                        }
                        if (debugging) {
                            Logger.debug(this, "speed: " + speed);
                        }

                        if (speed != 0) {
                            doSmoothScroll(speed, 30);
                        }
                    }
                    break;
            }
            return true;
        }
        return false;
    }

    /*
     * Restore size and visibility for all listitems
     */
    protected void unExpandViews(boolean deletion) {
        for (int i = 0;; i++) {
            View v = listView.getChildAt(i);
            if (v == null) {
                if (deletion) {
                    // HACK force update of mItemCount
                    int position = listView.getFirstVisiblePosition();
                    int y = listView.getChildAt(0).getTop();
                    if (listView.getAdapter() instanceof BaseAdapter) {
                        ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
                    }
//                    setAdapter(getAdapter());
                    draggableInterface.setSelectionFromTop(position, y);
                    // end hack
                }
                try {
                    listView.invalidate();
//                    layoutChildren(); // force children to be recreated where
                    // needed
                    v = listView.getChildAt(i);
                } catch (IllegalStateException ex) {
                    // layoutChildren throws this sometimes, presumably because
                    // we're
                    // in the process of being torn down but are still getting
                    // touch
                    // events
                }
                if (v == null) {
                    return;
                }
            }
            ViewGroup.LayoutParams params = v.getLayoutParams();
            v.setLayoutParams(params);
            v.setVisibility(View.VISIBLE);
        }
    }

    protected void startDragging(Bitmap bm, int x, int y) {
        stopDragging();

        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowParams.x = x - mDragPointX + mXOffset;
        mWindowParams.y = y - mDragPointY + mYOffset;

        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;

        ImageView v = new ImageView(context);
        v.setPadding(0, 0, 0, 0);
        v.setImageBitmap(bm);
        v.setAlpha(DRAG_VIEW_OPACITY);


        mWindowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(v, mWindowParams);
        mDragView = v;
    }

    protected void dragView(int x, int y) {
        if (mRemoveMode == REMOVE_MODE.SLIDE) {
            float alpha = 1.0f;
            int width = mDragView.getWidth();
            if (x > width / 2) {
                alpha = ((float) (width - x)) / (width / 2);
            }
            mWindowParams.alpha = alpha;
        }

        if (mRemoveMode == REMOVE_MODE.FLING || mRemoveMode == REMOVE_MODE.TRASH) {
            mWindowParams.x = x - mDragPointX + mXOffset;
        } else {
            mWindowParams.x = x;
//            mWindowParams.x = 0;
        }

        mWindowParams.y = y - mDragPointY + mYOffset;
        mWindowManager.updateViewLayout(mDragView, mWindowParams);

        if (mTrashcan != null) {
            int width = mDragView.getWidth();
            if (y > listView.getHeight() * 3 / 4) {
                mTrashcan.setLevel(2);
            } else if (width > 0 && x > width / 4) {
                mTrashcan.setLevel(1);
            } else {
                mTrashcan.setLevel(0);
            }
        }
    }

    protected void stopDragging() {
        if (mDragView != null) {
            mDragView.setVisibility(View.GONE);
            WindowManager wm =
                    (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mDragView);
            mDragView.setImageDrawable(null);
            mDragView = null;
        }
        if (mTrashcan != null) {
            mTrashcan.setLevel(0);
        }
    }

    public void doSmoothScroll(int aDistance, int aDuration) {
        // Fallback to scroll by if we can't smooth scroll.
        if (mSmoothScrollBy == null) {
            listView.scrollBy(0, aDistance);
            return;
        }

        try {
            mSmoothScrollBy.invoke(this, aDistance, aDuration);
        } catch (IllegalArgumentException e) {
            // No Exception Handling.
        } catch (IllegalAccessException e) {
            // No Exception Handling.
        } catch (InvocationTargetException e) {
            // No Exception Handling.
        }

    }

    protected void initMethods() {
        initSmoothScrollToPosition();
        initSetOverScrollMode();
        initSmoothScrollBy();

        // By default we disable all over scrolling.
        // TODO Re-enable later when we can override the yucky yucky
        // overscroll edging color!
        disableOverScroll();
    }

    protected void initSmoothScrollBy() {
        try {
            mSmoothScrollBy =
                    listClass.getMethod("smoothScrollBy", new Class[]{int.class, int.class});
        } catch (NoSuchMethodException e) {
            mSmoothScrollBy = null;
        }
    }

    protected void initSmoothScrollToPosition() {
        try {
            mSmoothScrollToPosition =
                    listClass.getMethod("smoothScrollToPosition", new Class[]{int.class});
        } catch (NoSuchMethodException e) {
            mSmoothScrollToPosition = null;
        }
    }

    protected void initSetOverScrollMode() {
        try {
            mSetOverScrollMethod =
                    listClass.getMethod("setOverScrollMode", new Class[]{int.class});
        } catch (NoSuchMethodException e) {
            mSetOverScrollMethod = null;
        }
    }

    protected void disableOverScroll() {
        if (mSetOverScrollMethod == null) {
            return;
        }

        try {
            // We can't actually completely disable it. If we do then flinging
            // the list down to the bottom makes it jump back to the middle.
            // This
            // is a bug in the ListView widget itself and can't be fixed at this
            // time. We'll revisit when the overscroll color becomes stylable or
            // when disabling overscrolling actually works without bugs.
            mSetOverScrollMethod.invoke(this, OVER_SCROLL_IF_CONTENT_SCROLLS);
        } catch (IllegalArgumentException e) {
            // No Exception Handling
        } catch (IllegalAccessException e) {
            // No Exception Handling
        } catch (InvocationTargetException e) {
            // No Exception Handling
        }
    }

    public void setSelection(int aPosition, boolean aAttemptSmoothScroll) {
        // If we can smooth scroll to the position do that instead of
        // simply setting the selection. Only smooth scroll if delta is smaller
        // or equal to the maximum distance we accept.
        int firstVisible = listView.getFirstVisiblePosition();
        int lastVisible = listView.getLastVisiblePosition();

        // Figure out the distance from either the first visible position
        // or last visible position depending on the requested position.
        // If aPosition is > firstVisible but < than lastVisible then it's
        // in the visible range and we don't have to worry.
        int distance = 0;
        if (aPosition < firstVisible) {
            distance = firstVisible - aPosition;
        } else if (aPosition > lastVisible) {
            distance = aPosition - lastVisible;
        }

        // TODO Figure out a way to always smooth scroll nicely.

        // Add the header views since scrolling doesn't do that for us.
        aPosition += draggableInterface.getHeaderViewsCount();

        if (aAttemptSmoothScroll &&
                mSmoothScrollToPosition != null &&
                distance <= MAX_SMOOTH_SCROLL_DISTANCE) {
            try {
                mSmoothScrollToPosition.invoke(this, aPosition);
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof Error) {
                    throw (Error) cause;
                } else {
                    throw new RuntimeException(ite);
                }
            } catch (IllegalAccessException ie) {
                // No Exception Handling.
            }
        } else {
            // Looks like we need to do it the old way :(
            listView.setSelection(aPosition);
        }
    }

    public void setTrashcan(Drawable trash) {
        mTrashcan = trash;
        mRemoveMode = REMOVE_MODE.TRASH;
    }

    public void setDragableListListener(DragableListListener l) {
        mDragableListListener = l;
    }
}

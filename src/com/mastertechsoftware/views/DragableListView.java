
package com.mastertechsoftware.views;

import android.content.Context;
import android.content.SharedPreferences;
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

import com.mastertechsoftware.AndroidUtil.R;
import com.mastertechsoftware.util.log.Logger;

public class DragableListView extends ListView {

    public static final int DRAGABLE_ICON_WIDTH = 86;

    private enum REMOVE_MODE {
        NONE,
        FLING,
        SLIDE,
        TRASH
    }
    private ImageView mDragView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    /**
     * At which position is the item currently being dragged. Note that this
     * takes in to account header items.
     */
    private int mDragPos;
    /**
     * At which position was the item being dragged originally
     */
    private int mSrcDragPos;
    private int mDragPointX; // at what x offset inside the item did the user
                             // grab
                             // it
    private int mDragPointY; // at what y offset inside the item did the user
                             // grab
                             // it
    private int mXOffset; // the difference between screen coordinates and
                          // coordinates in this view
    private int mYOffset; // the difference between screen coordinates and
                          // coordinates in this view
    private DragableListListener mDragableListListener;
    private int mUpperBound;
    private int mLowerBound;
    private int mHeight;
    private GestureDetector mGestureDetector;
    private REMOVE_MODE mRemoveMode = REMOVE_MODE.NONE;
    private Rect mTempRect = new Rect();
    private int mTouchSlop;
    private int mItemHeightNormal;
    private int mItemHeightHalf;
    private Drawable mTrashcan;
    private int dragableIconWidth = DRAGABLE_ICON_WIDTH;

    private final static int DRAG_VIEW_OPACITY = 229;

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
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mItemHeightNormal = -1;
        mItemHeightHalf = -1;
    }

    public void setDragableIconWidth(int dragableIconWidth) {
        this.dragableIconWidth = dragableIconWidth;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mDragableListListener != null && mGestureDetector == null) {
            if (mRemoveMode == REMOVE_MODE.FLING) {
                mGestureDetector = new GestureDetector(getContext(),
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
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();

                    // Make sure we're not trying to re-order an invalid item
                    // or a header view.
                    int itemnum = pointToPosition(x, y);
                    if (itemnum == AdapterView.INVALID_POSITION ||
                            itemnum < getHeaderViewsCount()) {
                        break;
                    }

                    ViewGroup item =
                            (ViewGroup) getChildAt(itemnum - getFirstVisiblePosition());

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
                        Bitmap bitmap = mDragableListListener.startDragging(x, y, item);
                        if (bitmap != null) {
                            startDragging(bitmap, x, y);
                        }
                        mDragPos = itemnum;
                        mSrcDragPos = mDragPos;
                        mHeight = getHeight();
                        int touchSlop = mTouchSlop;
                        mUpperBound = Math.min(y - touchSlop, mHeight / 3);
                        mLowerBound = Math.max(y + touchSlop, mHeight * 2 / 3);
                        return false;
                    }
                    stopDragging();
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    /*
     * pointToPosition() doesn't consider invisible views, but we need to, so
     * implement a slightly different version.
     */
    private int myPointToPosition(int x, int y) {

        if (y < 0) {
            // when dragging off the top of the screen, calculate position
            // by going back from a visible item
            int pos = myPointToPosition(x, y + mItemHeightNormal);
            if (pos > 0) {
                return pos - 1;
            }
        }

        Rect frame = mTempRect;
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            child.getHitRect(frame);
            if (frame.contains(x, y)) {
                return getFirstVisiblePosition() + i;
            }
        }
        return INVALID_POSITION;
    }

    private int getItemForPosition(int y) {
        int adjustedy = y - mDragPointY - mItemHeightHalf;
        int pos = myPointToPosition(0, adjustedy);
        if (pos >= 0) {
            pos += 1;
        } else if (adjustedy < 0) {
            // this shouldn't happen anymore now that myPointToPosition deals
            // with this situation
            pos = 0;
        }
        return pos;
    }

    private void adjustScrollBounds(int y) {
        if (y >= mHeight / 3) {
            mUpperBound = mHeight / 3;
        }
        if (y <= mHeight * 2 / 3) {
            mLowerBound = mHeight * 2 / 3;
        }
    }

    /*
     * Restore size and visibility for all listitems
     */
    private void unExpandViews(boolean deletion) {
        for (int i = 0;; i++) {
            View v = getChildAt(i);
            if (v == null) {
                if (deletion) {
                    // HACK force update of mItemCount
                    int position = getFirstVisiblePosition();
                    int y = getChildAt(0).getTop();
                    setAdapter(getAdapter());
                    setSelectionFromTop(position, y);
                    // end hack
                }
                try {
                    layoutChildren(); // force children to be recreated where
                                      // needed
                    v = getChildAt(i);
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

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mGestureDetector != null) {
            mGestureDetector.onTouchEvent(ev);
        }
        if ((mDragableListListener != null) && mDragView != null) {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Rect r = mTempRect;
                    mDragView.getDrawingRect(r);
                    stopDragging();
                    if (mRemoveMode == REMOVE_MODE.SLIDE && ev.getX() > r.right * 3 / 4) {
                        mDragableListListener.remove(mSrcDragPos);
                        unExpandViews(true);
                    } else {
                        int numheaders = getHeaderViewsCount();
                        Logger.debug(this, "Final Drag Position is " + mDragPos);
                        mDragPos = (mDragPos >= numheaders ? mDragPos : numheaders);
                        mDragPos = (mDragPos < getCount() ? mDragPos : getCount() - 1);
                        Logger.debug(this, "Final Drag Position is " + mDragPos);
                        mDragableListListener.drop(mSrcDragPos - numheaders,
                                mDragPos - numheaders);
                        unExpandViews(false);
                    }
                    break;

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    dragView(x, y);
                    int itemnum = getItemForPosition(y);
                    if (itemnum >= 0) {
                        if (action == MotionEvent.ACTION_DOWN || itemnum != mDragPos) {
                            int numheaders = getHeaderViewsCount();
                            if (mDragableListListener != null &&
                                    mDragPos >= numheaders &&
                                    mDragPos < getCount()) {
                                mDragableListListener.drag(mDragPos - numheaders,
                                        itemnum - numheaders);
                            }
                            mDragPos = itemnum;
                            Logger.debug(this, "Setting Drag Position to " + mDragPos);
                        }
                        int speed = 0;
                        adjustScrollBounds(y);
                        if (y > mLowerBound) {
                            // scroll the list up a bit
                            if (getLastVisiblePosition() < getCount() - 1) {
                                speed = y > (mHeight + mLowerBound) / 2 ? 16 : 4;
                            } else {
                                speed = 0;
                            }
                        } else if (y < mUpperBound) {
                            // scroll the list down a bit
                            speed = y < mUpperBound / 2 ? -16 : -4;
                            if (getFirstVisiblePosition() == 0
                                    && getChildAt(0).getTop() >= getPaddingTop()) {
                                // if we're already at the top, don't try to
                                // scroll, because
                                // it causes the framework to do some extra
                                // drawing that messes
                                // up our animation
                                speed = 0;
                            }
                        }

/*
                        if (speed != 0) {
                            doSmoothScroll(speed, 30);
                        }
*/
                    }
                    break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }

    private void startDragging(Bitmap bm, int x, int y) {
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

        Context context = getContext();
        ImageView v = new ImageView(context);
        v.setPadding(0, 0, 0, 0);
        v.setImageBitmap(bm);
        v.setAlpha(DRAG_VIEW_OPACITY);


        mWindowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(v, mWindowParams);
        mDragView = v;
    }

    private void dragView(int x, int y) {
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
            mWindowParams.x = 0;
        }

        mWindowParams.y = y - mDragPointY + mYOffset;
        mWindowManager.updateViewLayout(mDragView, mWindowParams);

        if (mTrashcan != null) {
            int width = mDragView.getWidth();
            if (y > getHeight() * 3 / 4) {
                mTrashcan.setLevel(2);
            } else if (width > 0 && x > width / 4) {
                mTrashcan.setLevel(1);
            } else {
                mTrashcan.setLevel(0);
            }
        }
    }

    private void stopDragging() {
        if (mDragView != null) {
            mDragView.setVisibility(GONE);
            WindowManager wm =
                    (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mDragView);
            mDragView.setImageDrawable(null);
            mDragView = null;
        }
        if (mTrashcan != null) {
            mTrashcan.setLevel(0);
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

package com.mastertechsoftware.views;

import android.R;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import com.mastertechsoftware.util.Logger;

/**
 * Date: Oct 27, 2010
 */
public class MasterListView extends ListView {
    protected int currentScrollState = OnScrollListener.SCROLL_STATE_IDLE;
    protected boolean scrolling = false;
    protected boolean pressed = false;
	private GestureDetector gestureDetector;
	private Handler handler = new Handler();
//	protected CheckForLongPress pendingCheckForLongPress;
	/**
	 * Whether the long press's action has been invoked.  The tap's action is invoked on the
	 * up event while a long press is invoked as soon as the long press duration is reached, so
	 * a long press could be performed before the tap is checked, in which case the tap's action
	 * should not be invoked.
	 */
	protected boolean hasPerformedLongPress = false;


    /**
     * Listener used to dispatch click events.
     * This field should be made protected, so it is hidden from the SDK.
     * {@hide}
     */
//	protected int motionPosition;
	protected OnItemClickListener onItemClickListener;


	public MasterListView(Context context) {
        this(context, null);
    }

    public MasterListView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.listViewStyle);
    }

    public MasterListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
		setItemsCanFocus(true);
		gestureDetector = new GestureDetector(context, new GestureListener());
//		gestureDetector.setIsLongpressEnabled(false);
//        setOnScrollListener(new OnScrollListener() {
//
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                currentScrollState = scrollState;
//                switch (scrollState) {
//                    case SCROLL_STATE_FLING:
//                    case SCROLL_STATE_TOUCH_SCROLL:
//                        scrolling = true;
////						Logger.debug("MasterListView", "onScrollStateChanged: scrolling");
//                        break;
//                }
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//
//            }
//        });
    }

/*
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
		if (!isEnabled()) {
			// A disabled view that is clickable still consumes the touch
			// events, it just doesn't respond to them.
			return isClickable() || isLongClickable();
		}
		final int action = ev.getAction();
		switch (action) {

			case MotionEvent.ACTION_DOWN:
				pressed = true;
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				pressed = false;
				break;
		}
		final int x = (int) ev.getX();
		final int y = (int) ev.getY();
        boolean result = super.onTouchEvent(ev);
        switch (action) {

            case MotionEvent.ACTION_DOWN:
                scrolling = false;
				int position = findMotionPosition(y);
//					Logger.debug("MasterListView", "onTouchEvent: ACTION_DOWN position=" + position);
//				int position = pointToPosition(x, y);
				if (position >= 0) {
					// Remember where the motion event started
					motionPosition = position;
				}
				if (isLongClickable()) {
//					Logger.debug("MasterListView", "onTouchEvent: postCheckForLongClick");
					postCheckForLongClick();
				}
                break;
            case MotionEvent.ACTION_UP:
				if (!hasPerformedLongPress) {
					// This is a tap, so remove the longpress check
//					Logger.debug("MasterListView", "onTouchEvent: ACTION_UP");
					removeLongPressCallback();
					if (!scrolling && (currentScrollState != OnScrollListener.SCROLL_STATE_FLING &&
						currentScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) ) {
//						Logger.debug("MasterListView", "onTouchEvent: scrolling");
						if (handleActionUp(y)) return true;
					}
				}
                scrolling = false;
                break;
        }
        return result;
    }
*/

	private View getView(int y) {
		// This count is just the # of visible items
		int childCount = getChildCount();
		if (childCount > 0) {
			for (int i = 0; i < childCount; i++) {
				View view = getChildAt(i);
				if (view == null) {
					continue;
				}
				if (y <= view.getBottom()) {
					return view;
				}
			}
		}
		return null;
	}

	private boolean handleActionUp(int y) {
		if (onItemClickListener != null) {
		   	final View view = getView(y);
			if (view != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						int position = getPositionForView(view);
						performItemClick(view, position, position);
					}
				});
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (gestureDetector.onTouchEvent(ev))
		{
			return true;
		}
/*
		final int action = ev.getAction();
		switch (action) {
			case MotionEvent.ACTION_UP:
				if (!scrolling && (currentScrollState != OnScrollListener.SCROLL_STATE_FLING &&
						currentScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)) {
					final int y = (int)ev.getY();
					View v = getView(y);
					if (v != null) {
//						super.dispatchTouchEvent(ev);
						if (handleActionUp(y)) {
							return true;
						}
					}
				}
				scrolling = false;
				break;
			case MotionEvent.ACTION_DOWN:
				scrolling = false;
				break;
		}
*/
		return super.dispatchTouchEvent(ev);
	}



	@Override
	public void setOnItemClickListener(OnItemClickListener listener) {
		super.setOnItemClickListener(listener);
		onItemClickListener = listener;
	}

/*
	protected void postCheckForLongClick() {
//		Logger.debug("MasterListView", "postCheckForLongClick");
		hasPerformedLongPress = false;

		if (pendingCheckForLongPress == null) {
			pendingCheckForLongPress = new CheckForLongPress();
		}
		pendingCheckForLongPress.rememberWindowAttachCount();
		postDelayed(pendingCheckForLongPress, ViewConfiguration.getLongPressTimeout());
	}

	protected int findMotionPosition(int y) {
		int childCount = getChildCount();
		if (childCount > 0) {
			for (int i = 0; i < childCount; i++) {
				View v = getChildAt(i);
				if (y <= v.getBottom()) {
					return getFirstVisiblePosition() + i;
				}
			}
			return getFirstVisiblePosition() + childCount - 1;
		}
		return INVALID_POSITION;
	}

*/
	/**
	 * Called by the view system when the focus state of this view changes.
	 * When the focus change event is caused by directional navigation, direction
	 * and previouslyFocusedRect provide insight into where the focus is coming from.
	 * When overriding, be sure to call up through to the super class so that
	 * the standard focus handling will occur.
	 *
	 * @param gainFocus True if the View has focus; false otherwise.
	 * @param direction The direction focus has moved when requestFocus()
	 *                  is called to give this view focus. Values are
	 *                  View.FOCUS_UP, View.FOCUS_DOWN, View.FOCUS_LEFT or
	 *                  View.FOCUS_RIGHT. It may not always apply, in which
	 *                  case use the default.
	 * @param previouslyFocusedRect The rectangle, in this view's coordinate
	 *        system, of the previously focused view.  If applicable, this will be
	 *        passed in as finer grained information about where the focus is coming
	 *        from (in addition to direction).  Will be <code>null</code> otherwise.
	 */
/*
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		if (!gainFocus) {
			removeLongPressCallback();
		}
	}
*/

	/**
	 * Changes the selection state of this view. A view can be selected or not.
	 * Note that selection is not the same as focus. Views are typically
	 * selected in the context of an AdapterView like ListView or GridView;
	 * the selected view is the view that is highlighted.
	 *
	 * @param selected true if the view must be selected, false otherwise
	 */
/*
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		if (isSelected() != selected) {
			if (!selected) removeLongPressCallback();
		}
	}
*/

/*
	protected void removeLongPressCallback() {
		if (!isEnabled()) {
			return;
		}

		if (!hasPerformedLongPress) {
			if (pendingCheckForLongPress != null) {
//				Logger.debug("MasterListView", "removeLongPressCallback");
				removeCallbacks(pendingCheckForLongPress);
				pendingCheckForLongPress = null;
			}
		}
	}
*/

	/**
	 * Default implementation of {@link android.view.KeyEvent.Callback#onKeyMultiple(int, int, android.view.KeyEvent)
	 * KeyEvent.Callback.onKeyMultiple()}: perform clicking of the view
	 * when {@link android.view.KeyEvent#KEYCODE_DPAD_CENTER} or
	 * {@link android.view.KeyEvent#KEYCODE_ENTER} is released.
	 *
	 * @param keyCode A key code that represents the button pressed, from
	 *                {@link android.view.KeyEvent}.
	 * @param event   The KeyEvent object that defines the button action.
	 */
/*
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER: {
				if (!isEnabled()) {
					return true;
				}
				if (isClickable() && pressed) {

					if (!hasPerformedLongPress) {
						// This is a tap, so remove the longpress check
						removeLongPressCallback();
//						Logger.debug("MasterListView", "onKeyUp");
					}
				}
				break;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
*/

/*
	class CheckForLongPress implements Runnable {

		private int mOriginalWindowAttachCount;

		public void run() {
			if (scrolling || (currentScrollState == OnScrollListener.SCROLL_STATE_FLING ||
				currentScrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) ) {
				return;
			}
			if (pressed && (getParent() != null)
					&& mOriginalWindowAttachCount == getWindowAttachCount()) {
				if (getOnItemLongClickListener() != null) {
//					Logger.debug("MasterListView", "CheckForLongPress: motionPosition=" + motionPosition);
					final long longPressId = getAdapter().getItemId(motionPosition);
//					Logger.debug("MasterListView", "CheckForLongPress: longPressId=" + longPressId);
					final View child = getChildAt(motionPosition - getFirstVisiblePosition());
					getOnItemLongClickListener().onItemLongClick(MasterListView.this, child, motionPosition,  longPressId);
				}
				if (performLongClick()) {
//					Logger.debug("MasterListView", "CheckForLongPress: performLongClick");
					hasPerformedLongPress = true;
				}
			}
		}

		public void rememberWindowAttachCount() {
			mOriginalWindowAttachCount = getWindowAttachCount();
		}
	}
*/
	/**
	 * Listener for user gestures. Specifically for swiping to go to the previous and next
	 * items in the media list
	 */
	private class GestureListener extends GestureDetector.SimpleOnGestureListener
	{
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Logger.debug("MasterListView onSingleTapConfirmed");
			final int y = (int)e.getY();
			View v = getView(y);
			if (v != null) {
//						super.dispatchTouchEvent(ev);
				if (handleActionUp(y)) {
					Logger.debug("MasterListView onSingleTapConfirmed - Handled");
					return true;
				}
			}
			return false;
		}
	}
}

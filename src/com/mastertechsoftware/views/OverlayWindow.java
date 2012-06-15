package com.mastertechsoftware.views;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * User: kevin.moore
 * Some code used from PopupWindow. Doesn't automatically close when clicking outside the window
 * and doesn't swallow the key events
 */
public class OverlayWindow {
//	private static String TAG = "OverlayWindow";
	private Context context;
	private WindowManager mWindowManager;
	private boolean mIsShowing;
	private View mContentView;
	private Drawable mBackground;
	private int mWindowLayoutType = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
	private View mPopupView;
	private int mWidth;
	private int mHeight;
	private int popupX;
	private int popupY;
	private OnDismissListener mOnDismissListener;
	private Rect hitRect = new Rect();
	private View.OnKeyListener mOnKeyListener;
	private View.OnTouchListener mOnTouchListener;

	/**
	 * Listener that is called when this popup window is dismissed.
	 */
	public interface OnDismissListener {
		/**
		 * Called when this popup window is dismissed.
		 */
		public boolean onDismiss();
	}

	/**
	 * Constructor. Context needed for window service
	 * @param context
	 */
	public OverlayWindow(Context context) {
		this.context = context;
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}

	/**
	 * <p>Return the drawable used as the popup window's background.</p>
	 *
	 * @return the background drawable or null
	 */
	public Drawable getBackground() {
		return mBackground;
	}

	public int getPopupX() {
		return popupX;
	}

	public int getPopupY() {
		return popupY;
	}

	/**
	 * <p>Change the background drawable for this popup window. The background
	 * can be set to null.</p>
	 *
	 * @param background the popup's background
	 */
	public void setBackgroundDrawable(Drawable background) {
		mBackground = background;
	}

	/**
	 * <p>Return the view used as the content of the popup window.</p>
	 *
	 * @return a {@link android.view.View} representing the popup's content
	 * @see #setContentView(android.view.View)
	 */
	public View getContentView() {
		return mContentView;
	}

	/**
	 * <p>Indicate whether this popup window is showing on screen.</p>
	 *
	 * @return true if the popup is showing, false otherwise
	 */
	public boolean isShowing() {
		return mIsShowing;
	}

	/**
	 * <p>Change the popup's content. The content is represented by an instance
	 * of {@link android.view.View}.</p>
	 * <p/>
	 * <p>This method has no effect if called when the popup is showing.  To
	 * apply it while a popup is showing, call </p>
	 *
	 * @param contentView the new content for the popup
	 * @see #getContentView()
	 * @see #isShowing()
	 */
	public void setContentView(View contentView) {
		if (isShowing()) {
			return;
		}

		mContentView = contentView;
	}

	/**
	 * <p>
	 * Display the content view in a popup window at the specified location. If the popup window
	 * cannot fit on screen, it will be clipped. See {@link android.view.WindowManager.LayoutParams}
	 * for more information on how gravity and the x and y parameters are related. Specifying
	 * a gravity of {@link android.view.Gravity#NO_GRAVITY} is similar to specifying
	 * <code>Gravity.LEFT | Gravity.TOP</code>.
	 * </p>
	 *
	 * @param parent  a parent view to get the {@link android.view.View#getWindowToken()} token from
	 * @param gravity the gravity which controls the placement of the popup window
	 * @param x	   the popup's x location offset
	 * @param y	   the popup's y location offset
	 */
	public void showAtLocation(View parent, int gravity, int x, int y) {
		if (isShowing() || mContentView == null) {
			return;
		}

		mIsShowing = true;

		WindowManager.LayoutParams p = createPopupLayout(parent.getWindowToken());

		if (mPopupView == null) {
			preparePopup(p);
		}
		if (gravity == Gravity.NO_GRAVITY) {
			gravity = Gravity.TOP | Gravity.LEFT;
		}
		p.gravity = gravity;
		p.x = x;
		p.y = y;
		popupX = x;
		popupY = y;
		invokePopup(p);
	}

	/**
	 * <p>Prepare the popup by embedding in into a new ViewGroup if the
	 * background drawable is not null. If embedding is required, the layout
	 * parameters' height is mnodified to take into account the background's
	 * padding.</p>
	 *
	 * @param p the layout parameters of the popup's content view
	 */
	private void preparePopup(WindowManager.LayoutParams p) {
		if (mContentView == null || context == null || mWindowManager == null) {
			throw new IllegalStateException("You must specify a valid content view by "
												+ "calling setContentView() before attempting to show the popup.");
		}

		if (mBackground != null) {
			final ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
			int height = ViewGroup.LayoutParams.MATCH_PARENT;
			if (layoutParams != null &&
				layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
				height = ViewGroup.LayoutParams.WRAP_CONTENT;
			}

			// when a background is available, we embed the content view
			// within another view that owns the background drawable
			PopupViewContainer popupViewContainer = new PopupViewContainer(context);
			PopupViewContainer.LayoutParams popupParams = new PopupViewContainer.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, height
			);
			popupViewContainer.setFocusable(false);
			popupViewContainer.setBackgroundDrawable(mBackground);
			popupViewContainer.addView(mContentView, popupParams);

			mPopupView = popupViewContainer;
		} else {
			mPopupView = mContentView;
		}
	}

	/**
	 * <p>Invoke the popup window by adding the content view to the window
	 * manager.</p>
	 * <p/>
	 * <p>The content view must be non-null when this method is invoked.</p>
	 *
	 * @param p the layout parameters of the popup's content view
	 */
	private void invokePopup(WindowManager.LayoutParams p) {
		p.packageName = context.getPackageName();
		mWindowManager.addView(mPopupView, p);
	}

	/**
	 * <p>Return this popup's height MeasureSpec</p>
	 *
	 * @return the height MeasureSpec of the popup
	 * @see #setHeight(int)
	 */
	public int getHeight() {
		return mHeight;
	}

	/**
	 * <p>Change the popup's height MeasureSpec</p>
	 * <p/>
	 * <p>If the popup is showing, calling this method will take effect only
	 * the next time the popup is shown.</p>
	 *
	 * @param height the height MeasureSpec of the popup
	 * @see #getHeight()
	 * @see #isShowing()
	 */
	public void setHeight(int height) {
		mHeight = height;
	}

	/**
	 * <p>Return this popup's width MeasureSpec</p>
	 *
	 * @return the width MeasureSpec of the popup
	 * @see #setWidth(int)
	 */
	public int getWidth() {
		return mWidth;
	}


	/**
	 * Get the Window's width
	 * @return width
	 */
	public int getWindowWidth() {
		return mPopupView.getWidth();
	}

	/**
	 * Get the Window's height
	 * @return height
	 */
	public int getWindowHeight() {
		return mPopupView.getHeight();
	}

	/**
	 * <p>Change the popup's width MeasureSpec</p>
	 * <p/>
	 * <p>If the popup is showing, calling this method will take effect only
	 * the next time the popup is shown.</p>
	 *
	 * @param width the width MeasureSpec of the popup
	 * @see #getWidth()
	 * @see #isShowing()
	 */
	public void setWidth(int width) {
		mWidth = width;
	}

	/**
	* Implement this method to handle touch screen motion events.
	*
	* @param event The motion event.
	* @return True if the event was handled, false otherwise.
	*/
	public boolean onTouchEvent(MotionEvent event) {
		return mPopupView.onTouchEvent(event);
	}

	/**
	 * <p>Generate the layout parameters for the popup window.</p>
	 *
	 * @param token the window token used to bind the popup's window
	 * @return the layout parameters to pass to the window manager
	 */
	private WindowManager.LayoutParams createPopupLayout(IBinder token) {
		// generates the layout parameters for the drop down
		// we want a fixed size view located at the bottom left of the anchor
		WindowManager.LayoutParams p = new WindowManager.LayoutParams();
		// these gravity settings put the view at the top left corner of the
		// screen. The view is then positioned to the appropriate location
		// by setting the x and y offsets to match the anchor's bottom
		// left corner
		p.gravity = Gravity.LEFT | Gravity.TOP;
		p.width = mWidth;
		p.height = mHeight;
		if (mBackground != null) {
			p.format = mBackground.getOpacity();
		} else {
			p.format = PixelFormat.TRANSLUCENT;
		}
		p.flags = computeFlags(p.flags);
		p.type = mWindowLayoutType;
		p.token = token;
		p.setTitle("OverlayWindow:" + Integer.toHexString(hashCode()));

		return p;
	}

	private int computeFlags(int curFlags) {
		curFlags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		curFlags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
		return curFlags;
	}

	/**
	 * <p>Dispose of the popup window.
	 */
	public void dismiss() {
		if (isShowing() && mPopupView != null) {

			try {
				mWindowManager.removeView(mPopupView);
			} finally {
				mIsShowing = false;

				if (mOnDismissListener != null) {
					mOnDismissListener.onDismiss();
				}
			}
		}
	}

	/**
	 * Set the Global Visable Rectangle
	 * @param rect
	 * @return true if rect is non-empty (i.e. part of the view is visible at the
	 *         root level.
	 */
	public boolean getGlobalVisibleRect(Rect rect) {
		return mPopupView.getGlobalVisibleRect(rect);
	}

	/**
	 * Sets the listener to be called when the window is dismissed.
	 *
	 * @param onDismissListener The listener.
	 */
	public void setOnDismissListener(OnDismissListener onDismissListener) {
		mOnDismissListener = onDismissListener;
	}

	/**
	 * Register a callback to be invoked when a key is pressed in this view.
	 * @param l the key listener to attach to this view
	 */
	public void setOnKeyListener(View.OnKeyListener l) {
		mOnKeyListener = l;
	}

	/**
	 * Register a callback to be invoked when a touch event is sent to this view.
	 * @param l the touch listener to attach to this view
	 */
	public void setOnTouchListener(View.OnTouchListener l) {
		mOnTouchListener = l;
	}

	/**
	 * FrameLayout. Can be expanded upon if needed.
	 */
	private class PopupViewContainer extends FrameLayout {

		public PopupViewContainer(Context context) {
			super(context);
		}

		/**
		 * This is needed so that the window doesn't send an automatic focus to the first item when
		 * the focus is moved to another window
		 * @param child
		 */
		@Override
		public void clearChildFocus(View child) {
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent event) {
			if (mOnTouchListener != null &&
					mOnTouchListener.onTouch(this, event)) {
				return true;
			}
//			Log.d(TAG, "OverlayWindow: dispatchTouchEvent");
			hitRect.set(popupX, popupY, popupX + getWindowWidth(), popupY + getWindowHeight());
//			Log.d(TAG, "OverlayWindow: Event X: " + (int)event.getRawX() + " Y: " + (int)event.getRawY());
//			Log.d(TAG, "OverlayWindow: HitRect " + hitRect);
			if (!hitRect.contains((int)event.getRawX(), (int)event.getRawY())) {
//				Log.d(TAG, "OverlayWindow: dispatchTouchEvent. Not handling");
				return false;
			}
			if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
				if (mOnDismissListener != null) {
					if (mOnDismissListener.onDismiss()) {
//						Log.d(TAG, "OverlayWindow: dispatchTouchEvent onDismiss handling");
						return true;
					}
				}
			}
//			Log.d(TAG, "OverlayWindow: dispatchTouchEvent sending to content view");
			mContentView.dispatchTouchEvent(event);
			return true;
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			if (mOnKeyListener != null
					&& mOnKeyListener.onKey(this, event.getKeyCode(), event)) {
				return true;
			}
			return super.dispatchKeyEvent(event);
		}
	}
}

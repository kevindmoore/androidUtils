
package com.mastertechsoftware.views;


import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Class for handling a OverlayWindow.
 */
public class PopupWindowWrapper {
    protected final View anchor;
    private final OverlayWindow window;
    private View root;
    private Drawable background = null;
    private final WindowManager windowManager;
    private boolean dismissPopupAutomatically = true;
    private OverlayWindow.OnDismissListener clientListener;

    /**
     * Create a PopupWindowWrapper
     * 
     * @param anchor the view that the PopupWindowWrapper will be displaying 'from'
     */
    public PopupWindowWrapper(View anchor) {
        this.anchor = anchor;
        window = new OverlayWindow(anchor.getContext());

        window.setOnDismissListener(new OverlayWindow.OnDismissListener() {
            @Override
            public boolean onDismiss() {
                if (clientListener != null) {
                    if (clientListener.onDismiss()) {
                        return true;
                    }
                }
                if (dismissPopupAutomatically) {
                    window.dismiss();
                    return true;
                }
                return false;
            }
        });

        windowManager = (WindowManager) this.anchor.getContext().getSystemService(
                Context.WINDOW_SERVICE);
        onCreate();
    }

    public void setDismissPopupAutomatically(boolean dismissPopupAutomatically) {
        this.dismissPopupAutomatically = dismissPopupAutomatically;
    }

    /**
     * Register a callback to be invoked when a key is pressed in this view.
     * 
     * @param l the key listener to attach to this view
     */
    public void setOnKeyListener(View.OnKeyListener l) {
        window.setOnKeyListener(l);
    }

    /**
     * Register a callback to be invoked when a touch event is sent to this view.
     * 
     * @param l the touch listener to attach to this view
     */
    public void setOnTouchListener(View.OnTouchListener l) {
        window.setOnTouchListener(l);
    }

    /**
     * Is the popup window showing?
     * 
     * @return
     */
    public boolean isShowing() {
        return window.isShowing();
    }

    /**
     * Set the Global Visable Rectangle
     * 
     * @param rect
     * @return true if rect is non-empty (i.e. part of the view is visible at the root level.
     */
    public boolean getGlobalVisibleRect(Rect rect) {
        if (!window.isShowing()) {
            return false;
        }
        rect.set(window.getPopupX(), window.getPopupY(),
                window.getPopupX() + window.getWindowWidth(),
                window.getPopupY() + window.getWindowHeight());
        return true;
    }

    /**
     * Implement this method to handle touch screen motion events.
     * 
     * @param event The motion event.
     * @return True if the event was handled, false otherwise.
     */
    public boolean onTouchEvent(MotionEvent event) {
        return window.onTouchEvent(event);
    }

    /**
     * Anything you want to have happen when created. Probably should create a view and setup the
     * event listeners on child views.
     */
    protected void onCreate() {
    }

    /**
     * In case there is stuff to do right before displaying.
     */
    protected void onShow() {
    }

    private void preShow() {
        if (root == null) {
            throw new IllegalStateException("setContentView was not called with a view to display.");
        }
        onShow();

        if (background != null) {
            window.setBackgroundDrawable(background);
        } else {
            window.setBackgroundDrawable(null);
        }
        setWindowSize();

    }

    private void setWindowSize() {
        // if using PopupWindow#setBackgroundDrawable this is the only values of the width and
        // height that make it work
        // otherwise you need to set the background of the root viewgroup
        // and set the popupwindow background to an empty BitmapDrawable
        if (root.getWidth() > 0) {
            window.setHeight(root.getMeasuredHeight());
            window.setWidth(root.getMeasuredWidth());
        } else {
            ViewGroup.LayoutParams layoutParams = root.getLayoutParams();
            if (layoutParams == null) {
                root.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        }
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();

        int rootWidth = Math.min(root.getMeasuredWidth(), screenWidth);
        int rootHeight = Math.min(root.getMeasuredHeight(), screenHeight);
        window.setHeight(rootHeight);
        window.setWidth(rootWidth);
    }

    /**
     * Set the Popup Window size
     * 
     * @param width
     * @param height
     */
    public void setWindowSize(int width, int height) {
        window.setWidth(width);
        window.setHeight(height);
    }

    /**
     * Background Drawable
     * 
     * @param background
     */
    public void setBackgroundDrawable(Drawable background) {
        this.background = background;
    }

    public void setWindowBackground(Drawable background) {
        window.setBackgroundDrawable(background);
    }

    /**
     * Sets the content view. Probably should be called from onCreate
     * 
     * @param root the view the popup will display
     */
    public void setContentView(View root) {
        this.root = root;
        window.setContentView(root);
    }

    /**
     * Will inflate and set the view from a resource id
     * 
     * @param layoutResID
     */
    public void setContentView(int layoutResID) {
        LayoutInflater inflator = (LayoutInflater) anchor.getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        setContentView(inflator.inflate(layoutResID, null));
    }

    /**
     * If you want to do anything when dismiss is called
     * 
     * @param listener
     */
    public void setOnDismissListener(OverlayWindow.OnDismissListener listener) {
        clientListener = listener;
    }

    /**
     * Display window at with zero offset.
     */
    public void showWindow() {
        showWindow(0, 0);
    }

    /**
     * Position and display window.
     * 
     * @param xOffset offset in the X direction
     * @param yOffset offset in the Y direction
     */
    public void showWindow(int xOffset, int yOffset) {
        if (isShowing()) {
            return;
        }
        preShow();

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);

        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();

        int rootWidth = Math.min(root.getMeasuredWidth(), screenWidth);
        int rootHeight = Math.min(root.getMeasuredHeight(), screenHeight);

        int xPos = ((screenWidth - rootWidth) / 2) + xOffset;
        int yPos = ((screenHeight - rootHeight) / 2) + yOffset;

        Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(),
                location[1] + anchor.getHeight());
        yPos = anchorRect.top - rootHeight + yOffset;

        // display on bottom
        if (rootHeight > anchorRect.top) {
            yPos = anchorRect.bottom + yOffset;
        }

        window.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
    }

    /**
     * Show the window at the given position. Don't do any positioning calculations
     * 
     * @param xPos
     * @param yPos
     */
    public void showWindowAt(int xPos, int yPos) {
        if (isShowing()) {
            return;
        }
        if (window.getWidth() == 0) {
            window.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        }
        window.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
    }

    /**
     * Dismiss the popup window
     */
    public void dismiss() {
        window.dismiss();
    }
}

package com.mastertechsoftware.activity;

import com.mastertechsoftware.AndroidUtil.R;
import com.mastertechsoftware.util.ExceptionHandler;
import com.mastertechsoftware.util.StackTraceOutput;
import com.mastertechsoftware.util.log.Logger;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.List;

/**
 * User: kevin.moore
 */
public abstract class FlipperActivity extends Activity {
	// Swipe constants
	public static final int DIALOG_PROGRESS_ID = 0;
	public static final String ACTIVITY_EXTRA = "com.mastertechsoftware.activity";

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_VELOCITY = 200; // pixels per seconds
	private int REL_SWIPE_MIN_DISTANCE ;
	private int REL_SWIPE_MAX_OFF_PATH;
	private int REL_SWIPE_THRESHOLD_VELOCITY;
	protected ViewFlipper flipper;
	protected int flipperCount = 0;
	protected int currentFlipperPosition = -1;
	protected Animation inFromLeft;
	protected Animation outFromRight;
	protected Animation outFromLeft;
	protected Animation inFromRight;
	protected GestureDetector gestureDetector;
	protected GestureListener gestureListener;
	protected ActivityListener currentActivityListener;
	protected Handler handler = new Handler();
	protected List<ActivityListener> activityListenerList = new ArrayList<ActivityListener>();
	protected List<ActivityListener> allActivities = new ArrayList<ActivityListener>();
	protected boolean handlingBack = false;
	protected boolean debugging = false;
	private int[] location = new int[2];
	private final Rect mTempRect = new Rect();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
//		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

		DisplayMetrics dm = getResources().getDisplayMetrics();
		REL_SWIPE_MIN_DISTANCE = SWIPE_MIN_DISTANCE * dm.densityDpi / 160;
		REL_SWIPE_MAX_OFF_PATH = SWIPE_MAX_OFF_PATH * dm.densityDpi / 160;
		REL_SWIPE_THRESHOLD_VELOCITY = SWIPE_VELOCITY * dm.densityDpi / 160;
		gestureListener = new GestureListener();
		gestureDetector = new GestureDetector(this, gestureListener);
		gestureDetector.setIsLongpressEnabled(true);
		flipper = new ViewFlipper(this);
		// Set-up the animation
		inFromLeft = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
		outFromRight = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
		outFromLeft = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
		inFromRight = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);

		flipper.setInAnimation(inFromRight);
		flipper.setOutAnimation(outFromLeft);
		setContentView(flipper);
		createStartActivity();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (currentActivityListener != null) {
			currentActivityListener.configurationChanged(newConfig);
		}
	}

	/**
	 * Start a new activity. Add it to our list and flip to it
	 * @param activityListener
	 */
	public void startActivity(ActivityListener activityListener) {
		if (activityListener == null) {
			Logger.error("StartActivity listener is null");
			return;
		}
		hideKeyboard();
		if (currentActivityListener != null && activityListener != currentActivityListener) {
			currentActivityListener.pause();
		}
		currentActivityListener = activityListener;


		int foundPosition = findActivityPosition(activityListener);
		flipper.setInAnimation(inFromRight);
		flipper.setOutAnimation(outFromLeft);

		if (foundPosition == -1) {
			currentFlipperPosition++;
		} else {
			currentFlipperPosition = foundPosition;
		}
		removeFlipperViews(currentFlipperPosition);
		if (debugging)
			Logger.debug("startActivity currentFlipperPosition " + currentFlipperPosition);

		// Not added yet
		if (foundPosition == -1) {
			View view = null;
			if (activityExists(currentActivityListener)) {
				view = currentActivityListener.getCurrentView();
			} else {
				view = currentActivityListener.createView(this);
				currentActivityListener.setCurrentView(view);
				allActivities.add(currentActivityListener);
			}
			if (activityListenerList.size() <= currentFlipperPosition) {
				activityListenerList.add(currentActivityListener);
			} else {
				activityListenerList.set(currentFlipperPosition, currentActivityListener);
			}
			if (view != null) {
				flipper.addView(view,
								new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
														   ViewGroup.LayoutParams.MATCH_PARENT));
				flipper.setDisplayedChild(currentFlipperPosition);
				flipperCount++;
			}
		} else {
			if (debugging)
				Logger.debug("startActivity activity found at " + foundPosition);
			//			ActivityListener oldActivity = activityListenerList.get(currentFlipperPosition);
			//			if (currentActivityListener != oldActivity) {
			//				if (oldActivity != null) {
			//					oldActivity.destroy();
			//				}
			//			}
			activityListenerList.set(currentFlipperPosition, currentActivityListener);
			View view = currentActivityListener.getCurrentView();
			if (view != null) {
				flipper.addView(view,
								new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
														   ViewGroup.LayoutParams.MATCH_PARENT));
				flipper.setDisplayedChild(currentFlipperPosition);
			}
			currentActivityListener.resume();
		}
	}

    /**
     * Replace the current Activities's view
     * @param oldView
     * @param newView
     */
    public void replaceView(View oldView, View newView) {
        flipper.removeView(oldView);
        flipper.addView(newView,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
    }

	/**
	 * Start activity with given bundle
	 * @param activityListener
	 * @param params
	 */
	public void startActivity(ActivityListener activityListener, Bundle params) {
		activityListener.setParams(params);
		startActivity(activityListener);
	}

	/**
	 * Start the given activity with the given params
	 * @param activity
	 * @param params
	 */
	public void startActivity(String activity, Bundle params) {
		int position = getPositionForActivity(activity);
		ActivityListener activityListener = createActivity(position);
		if (activityListener == null) {
			Logger.error("startActivity:Activity not found for position " + position);
			return;
		}
		activityListener.setParams(params);
		startActivity(activityListener);
		activityListener.startup();
	}
	
	/**
	 * Remove all views from the starting position
	 * @param startPos
	 */
	protected void removeFlipperViews(int startPos) {
		if (startPos < flipper.getChildCount() && startPos >= 0) {
			// Start, count
			flipper.removeViews(startPos, (flipper.getChildCount() - startPos));
		}
		flipperCount = flipper.getChildCount();
	}

	/**
	 * Show the next activity if it exists.
	 */
	public void showNextActivity() {
		if (debugging)
			Logger.debug("showNextActivity: currentFlipperPosition=" + currentFlipperPosition);
		hideKeyboard();
		if (currentFlipperPosition < (flipper.getChildCount()-1)) {
			flipper.setInAnimation(inFromRight);
			flipper.setOutAnimation(outFromLeft);
			if (currentActivityListener != null) {
				currentActivityListener.pause();
			}
			currentFlipperPosition++;
			currentActivityListener = activityListenerList.get(currentFlipperPosition);
			flipper.setDisplayedChild(currentFlipperPosition);
			if (currentActivityListener != null) {
				currentActivityListener.resume();
			}
		}

	}

	/**
	 * Show previous activity if it exists
	 */
	public void showPreviousActivity() {
		if (debugging)
			Logger.debug("showPreviousActivity: currentFlipperPosition=" + currentFlipperPosition);
		hideKeyboard();
		if (currentFlipperPosition > 0) {
			flipper.setInAnimation(inFromLeft);
			flipper.setOutAnimation(outFromRight);
			if (currentActivityListener != null) {
				currentActivityListener.pause();
			}
			currentFlipperPosition--;
			currentActivityListener = activityListenerList.get(currentFlipperPosition);
			flipper.setDisplayedChild(currentFlipperPosition);
			if (currentActivityListener != null) {
				currentActivityListener.resume();
			}
		}
	}

	/**
	 * When 1 "activity" finishes with a result, pass it to the previous activity
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public void finishActivity(int requestCode, int resultCode, Intent data) {
		hideKeyboard();
		flipper.setInAnimation(inFromLeft);
		flipper.setOutAnimation(outFromRight);
		if (currentFlipperPosition > 0) {
			if (currentActivityListener != null) {
				currentActivityListener.pause();
			}
			currentFlipperPosition--;
			currentActivityListener = activityListenerList.get(currentFlipperPosition);
			flipper.setDisplayedChild(currentFlipperPosition);
			if (currentActivityListener != null) {
				currentActivityListener.activityResult(requestCode, resultCode, data);
			}
		}
	}

	/**
	 * Find the given activity in our list
	 * @param activityListener
	 * @return position
	 */
	protected int findActivityPosition(ActivityListener activityListener) {
		int position = 0;
		for (ActivityListener listener : activityListenerList) {
			if (listener == activityListener) {
				return position;
			}
			position++;
		}
		return -1;
	}

	/**
	 * Find the given activity in our list
	 * @param activity
	 * @return position
	 */
	protected ActivityListener findActivity(String activity) {
		for (ActivityListener listener : activityListenerList) {
			if (activity.equalsIgnoreCase(listener.getActivityName())) {
				return listener;
			}
		}
		return null;
	}

	/**
	 * See if the given activity already exists
	 * @param activityListener
	 * @return true if already created
	 */
	protected boolean activityExists(ActivityListener activityListener) {
		for (ActivityListener listener : allActivities) {
			if (listener == activityListener) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Find the position of the activity in the list. -1 if not found
	 * @param activity
	 * @return  position
	 */
	protected int getActivityPosition(String activity) {
		int position = 0;
		for (ActivityListener listener : activityListenerList) {
			if (activity.equalsIgnoreCase(listener.getActivityName())) {
				return position;
			}
			position++;
		}
		return -1;
	}

	@Override
	protected void onPause() {
		if (currentActivityListener != null) {
			currentActivityListener.pause();
		}
		super.onPause();
	}

	/**
	 * Create the starting activity
	 */
	protected abstract void createStartActivity();

	@Override
	protected void onResume() {
		super.onResume();
		if (currentActivityListener != null) {
			currentActivityListener.resume();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			String activity = (String) extras.get(ACTIVITY_EXTRA);
			if (activity != null){
				if (debugging)
					Logger.debug("FlipperActivity:activity " + activity);
				ActivityListener activityListener = findActivity(activity);
				if (activityListener != null && activityListener != currentActivityListener) {
					int position = getActivityPosition(activity);
					if (position <= 0) {
						currentFlipperPosition = 0;
					} else {
						currentFlipperPosition = position - 1;
					}
					if (debugging)
						Logger.debug("FlipperActivity:starting activity at " + currentFlipperPosition);
					startActivity(activityListener, extras);
					activityListener.resume();
				} else if (activityListener != currentActivityListener) {
					startActivity(activity, extras);
				} else if (activityListener == null) {
					Logger.error("FlipperActivity:starting activity with no listener found ");
					startActivity(activity, extras);
				}
			}
		}
		if (currentActivityListener != null) {
			ActivityListener nextActivity = currentActivityListener.handleNewIntent(intent);
			if (nextActivity != null) {
				startActivity(nextActivity);
			}
		}
	}

	@Override
	protected void onDestroy() {
		for (ActivityListener activityListener : activityListenerList) {
			if (activityListener != null) {
				activityListener.destroy();
			}
		}
		activityListenerList.clear();
		allActivities.clear();
		super.onDestroy();
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
			case DIALOG_PROGRESS_ID: {
				// Don't dim the back of the dialog to show the nice animation
				ProgressDialog dialog = new ProgressDialog(this);
				dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
				dialog.setMessage(getResources().getString(R.string.loading));
				dialog.setIndeterminate(true);
				return dialog;
			}

		}
		return currentActivityListener.createDialog(id);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
//		menu.removeGroup(0);
		if (currentActivityListener != null) {
			currentActivityListener.createOptionsMenu(menu);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		if (currentActivityListener != null) {
			currentActivityListener.createContextMenu(menu, v, menuInfo);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (debugging)
			Logger.debug("onKeyDown: keyCode " + keyCode);
		if (keyCode == KeyEvent.KEYCODE_BACK && currentFlipperPosition > 0) {
			event.startTracking();
			handlingBack = true;
			if (currentActivityListener != null) {
				if (currentActivityListener.onBack()) {
					if (debugging)
						Logger.debug("onKeyDown: currentActivityListener.onBack ");
					return true;
				}
			}
			if (debugging)
				Logger.debug("onKeyDown: showPreviousActivity ");
			showPreviousActivity();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK && currentFlipperPosition == 0) {
			handlingBack = true;
			if (debugging)
				Logger.debug("onKeyDown: Finishing ");
			finish();
			return true;
		}
		if (currentActivityListener != null && currentActivityListener.keyDown(keyCode, event)) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (debugging)
			Logger.debug("onKeyUp: keyCode " + keyCode);
		if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
			&& !event.isCanceled() && handlingBack) {
			if (debugging)
				Logger.debug("onKeyUp: Handling back ");
			handlingBack = false;
			return true;
		}
		handlingBack = false;

		if (currentActivityListener != null && currentActivityListener.keyUp(keyCode, event)) {
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (currentActivityListener != null) {
			currentActivityListener.activityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// Check with our gesture listener first
		if (gestureDetector.onTouchEvent(event))
		{
			//			Logger.debug("FlipperActivity:dispatchTouchEvent handled");
			return true;
		}
		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (currentActivityListener != null && currentActivityListener.touchEvent(event)) {
			return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (currentActivityListener != null && currentActivityListener.optionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (currentActivityListener != null && currentActivityListener.contextItemSelected(item)) {
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * Start a new activity
	 * @param position
	 */
	public void startActivity(int position) {
		ActivityListener activity = createActivity(position);
		if (activity == null) {
			Logger.error("startActivity:Activity not found for position " + position);
			Logger.error(StackTraceOutput.getStackTrace(4));
			return;
		}
		startActivity(activity);
		activity.startup();
	}


	/**
	 * Start an activity with params
	 * @param position
	 * @param params
	 */
	public void startActivity(int position, Bundle params) {
		ActivityListener activity = createActivity(position);
		activity.setParams(params);
		startActivity(activity);
		activity.startup();
	}

	/**
	 * Start an activity with params
	 * @param position
	 * @param requestCode
	 * @param params
	 */
	public void startActivity(int position, int requestCode, Bundle params) {
		ActivityListener activity = createActivity(position);
		if (activity == null) {
			Logger.error("startActivity:Activity not found for position " + position);
			return;
		}
		activity.setRequestCode(requestCode);
		activity.setParams(params);
		startActivity(activity);
		activity.startup();
	}

	/**
	 * Create a new activity
	 * @param position
	 * @return ActivityListener
	 */
	public abstract ActivityListener createActivity(int position);

	/**
	 * For the given activity string, return the position needed to create the activity.
	 * @param activity
	 * @return position
	 */
	public abstract int getPositionForActivity(String activity);

	/**
	 * Start a new activity
	 * @param activity
	 * @return true if started
	 */
	public abstract boolean startActivity(String activity);

	public int getListViewPosition(ListView listView, MotionEvent event) {
		listView.getLocationOnScreen(location);
		location[0] = (int) event.getX() - location[0];
		location[1] = (int) event.getY() - location[1];
		//		Logger.debug("FlipperActivity:getListViewPosition new x = " + location[0] + " new y = " + location[1]);
		final int count = listView.getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = listView.getChildAt(i);
			if (child.getVisibility() == View.VISIBLE) {
				child.getHitRect(mTempRect);
				if (location[1] <= child.getBottom()) {
					return i + listView.getFirstVisiblePosition();
				}
			}
		}
		return -1;
	}

	public View getListView(ListView listView, int position) {
		if (position >= 0 && position < listView.getChildCount()) {
			return listView.getChildAt(position);
		}
		return null;
	}

	/**
	 * Listener for user gestures. Specifically for swiping to go to the previous and next
	 * items in the media list
	 */
	private class GestureListener extends GestureDetector.SimpleOnGestureListener {
		protected boolean processingFling = false;

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			//			Logger.debug("onSingleTapUp");
			if (processingFling) {
				if (debugging)
					Logger.debug("onSingleTapUp - processing");
				return false;
			}
			boolean result = false;
			if (currentActivityListener != null) {
				result = currentActivityListener.tapUpEvent(e);
			}
			//			Logger.debug("FlipperActivity:onSingleTapUp Handled = " + (result ? "true" : "false"));
			return result;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			//			Logger.debug("onLongPress");
			if (processingFling) {
				if (debugging)
					Logger.debug("onLongPress - processing");
				return;
			}
			if (currentActivityListener != null) {
				currentActivityListener.longPressEvent(e);
			}
			//			Logger.debug("FlipperActivity:onLongPress Handled = " + (result ? "true" : "false"));
		}


		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

			processingFling = false;
			// Catch possible NPE
			if (e1 == null || e2 == null) return false;

			// Compute the fling distance
			float leftXDistance = e1.getX() - e2.getX();
			float rightXDistance = e2.getX() - e1.getX();
			float yDistance = Math.abs(e1.getY() - e2.getY());

			if (yDistance > REL_SWIPE_MAX_OFF_PATH)
				return false;
			else {
				// If the distance and velocity exceed our swiping detection thresholds,
				// then do the swipe!

				if (leftXDistance > REL_SWIPE_MIN_DISTANCE && Math.abs(velocityX) >= REL_SWIPE_THRESHOLD_VELOCITY) {
					processingFling = true;
					//					Logger.debug("Swipe right detected");
					handler.post(new Runnable() {
						@Override
						public void run() {
							showNextActivity();
						}
					});
					return true;
				} else if (rightXDistance > REL_SWIPE_MIN_DISTANCE && Math.abs(velocityX) >= REL_SWIPE_THRESHOLD_VELOCITY) {
					processingFling = true;
					//					Logger.debug("Swipe left detected");
					handler.post(new Runnable() {
						@Override
						public void run() {
							showPreviousActivity();
						}
					});
					return true;
				}

				/*
					if (Math.abs(velocityX) >= SWIPE_VELOCITY) {
						if (leftXDistance < SWIPE_LEFT) {
							processingFling = true;
							Logger.debug("Swipe left detected");
							handler.post(new Runnable() {
								@Override
								public void run() {
									showPreviousActivity();
								}
							});
							return true;
						} else if (leftXDistance > SWIPE_RIGHT) {
							processingFling = true;
							Logger.debug("Swipe right detected");
							handler.post(new Runnable() {
								@Override
								public void run() {
									showNextActivity();
								}
							});
							return true;
						}
					}
	*/

				return false;
			}
		}

	}


	/**
	 * Hide any keyboard that might be open
	 */
	public void hideKeyboard() {
		InputMethodManager inputMethodService = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodService != null && inputMethodService.isActive()) {
			inputMethodService.hideSoftInputFromWindow(
				getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
		}
	}

	public static void hideKeyboard(Activity activity) {
		InputMethodManager inputMethodService = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodService != null && inputMethodService.isActive()) {
			inputMethodService.hideSoftInputFromWindow(
                    activity.getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
		}
	}

	public static void hideKeyboard(Activity activity, View view) {
		InputMethodManager inputMethodService = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodService != null && inputMethodService.isActive()) {
			inputMethodService.hideSoftInputFromWindow(
                    view.getWindowToken(), 0);
		}
	}
	public static void showKeyboard(Activity activity, View view) {
		InputMethodManager inputMethodService = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodService != null && inputMethodService.isActive()) {
			inputMethodService.showSoftInput(view, 0);
		}
	}
}

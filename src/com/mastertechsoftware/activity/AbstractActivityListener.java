package com.mastertechsoftware.activity;

/**
 * User: kevin.moore
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

/**
 * Abstract Activity Listener
 */
public abstract class AbstractActivityListener implements ActivityListener  {
	protected View currentView;
	protected Bundle params;
	protected boolean paused = true;
	protected int requestCode;
    protected FlipperActivity mainActivity;

    public AbstractActivityListener(FlipperActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public abstract String getActivityName();

	@Override
	public void startup() {
	}

    public FlipperActivity getMainActivity() {
        return mainActivity;
    }

    @Override
	public void configurationChanged(Configuration newConfig) {
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public int getRequestCode() {
		return requestCode;
	}

	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}

	public boolean onBack() {
		return false;
	}


	public Bundle getParams() {
		return params;
	}

	public void setParams(Bundle params) {
		this.params = params;
	}

	@Override
	public abstract View createView(Activity context);

	@Override
	public View getCurrentView() {
		return currentView;
	}

	@Override
	public Dialog createDialog(int id) {
		return null;
	}

	public void setCurrentView(View currentView) {
		this.currentView = currentView;
	}

	@Override
	public void pause() {
		paused = true;
	}


	@Override
	public void resume() {
		paused = false;
	}

	@Override
	public ActivityListener handleNewIntent(Intent intent) {
		return null;
	}

	@Override
	public void destroy() {

	}

	@Override
	public boolean createOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	public boolean createContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		return false;
	}

	@Override
	public boolean keyDown(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public boolean keyUp(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public boolean touchEvent(MotionEvent event) {
		return false;
	}

	@Override
	public boolean tapUpEvent(MotionEvent event) {
		return false;
	}

	@Override
	public boolean longPressEvent(MotionEvent event) {
		return false;
	}

	@Override
	public boolean optionsItemSelected(MenuItem item) {
		return false;
	}

	@Override
	public boolean contextItemSelected(MenuItem item) {
		return false;
	}

	@Override
	public void activityResult(int requestCode, int resultCode, Intent data) {

	}
}

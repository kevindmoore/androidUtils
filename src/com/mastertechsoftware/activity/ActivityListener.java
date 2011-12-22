package com.mastertechsoftware.activity;

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
 * @author Kevin Moore
 */
public interface ActivityListener {
	String getActivityName();

	View getCurrentView();

	View createView(Activity context);

	void setRequestCode(int requestCode);

	boolean onBack();

	Bundle getParams();

	void setParams(Bundle params);

	Dialog createDialog(int id);

	void startup();

	void pause();

	void resume();

	ActivityListener handleNewIntent(Intent intent);

	void destroy();

	boolean createOptionsMenu(Menu menu);

	boolean createContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);

	void configurationChanged(Configuration newConfig);

	boolean keyDown(int keyCode, KeyEvent event);

	boolean keyUp(int keyCode, KeyEvent event);

	boolean touchEvent(MotionEvent event);

	boolean tapUpEvent(MotionEvent event);

	boolean longPressEvent(MotionEvent event);

	boolean optionsItemSelected(MenuItem item);

	boolean contextItemSelected(MenuItem item);

	void activityResult(int requestCode, int resultCode, Intent data);

	void setCurrentView(View view);
}

package com.mastertechsoftware.ui;

import android.support.v4.app.Fragment;
/**
 * Listener for page events
 */
public interface FragmentListener {
	void pageChanged(int position, Fragment fragment);
}

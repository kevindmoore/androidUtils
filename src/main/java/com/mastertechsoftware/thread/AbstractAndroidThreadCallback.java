package com.mastertechsoftware.thread;

import com.mastertechsoftware.util.log.Logger;
/**
 * Abstract Thread callback so that you don't have to implement every method.
 */
public abstract class AbstractAndroidThreadCallback<T> implements AndroidThreadCallback<T> {

	@Override
	public void doOnMainThread(T result) {

	}

	@Override
	public void handleError(Exception e) {
		Logger.error(e);
	}
}

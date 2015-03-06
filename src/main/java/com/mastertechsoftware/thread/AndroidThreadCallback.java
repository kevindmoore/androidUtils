package com.mastertechsoftware.thread;

/**
 * Callback for AndroidThreadHandler
 */
public interface AndroidThreadCallback<T> {
	T doInBackground();
	void doOnMainThread(T result);
	void handleError(Exception e);
}

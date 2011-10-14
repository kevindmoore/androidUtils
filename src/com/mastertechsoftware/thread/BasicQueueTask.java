package com.mastertechsoftware.thread;

import android.app.Activity;

/**
 * User: kevin.moore
 */
public interface BasicQueueTask<QueueItem> {

	void init(QueueItem item);
	void process();
	void finished();
	Activity getActivity();
}

package com.mastertechsoftware.thread;

import android.app.Activity;

/**
 * User: kevin.moore
 */
public interface BasicQueueTask<QueueItem> {

	void process(QueueItem item);
	void finished();
	Activity getActivity();
}

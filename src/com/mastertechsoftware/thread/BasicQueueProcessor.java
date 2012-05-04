package com.mastertechsoftware.thread;

import android.app.Activity;

import com.mastertechsoftware.util.log.Logger;

/**
 * User: kevin.moore
 */
public class BasicQueueProcessor<QueueItem> extends QueueProcessor<BasicQueue<QueueItem>> {

	@Override
	protected void process(final BasicQueue<QueueItem> queueItem) {
		try {
			queueItem.getQueueTask().process(queueItem.getData());
		} catch (Exception e) {
			Logger.error("BasicQueueProcessor:process", e);
		}
        Activity activity = queueItem.getQueueTask().getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        queueItem.getQueueTask().finished();
                    } catch (Exception e) {
                        Logger.error("BasicQueueProcessor:runOnUiThread", e);
                    }
                }
            });
        }
	}

	@Override
	protected void finish() {
	}
}

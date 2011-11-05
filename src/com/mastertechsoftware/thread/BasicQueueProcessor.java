package com.mastertechsoftware.thread;

import com.mastertechsoftware.util.log.Logger;

/**
 * User: kevin.moore
 */
public class BasicQueueProcessor<QueueItem> extends QueueProcessor<BasicQueueTask<QueueItem>> {

	@Override
	protected void process(final BasicQueueTask<QueueItem> queueTask) {
//		runOnce = true;
		try {
			queueTask.process();
		} catch (Exception e) {
			Logger.error("BasicQueueProcessor:process", e);
		}
		queueTask.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					queueTask.finished();
				} catch (Exception e) {
					Logger.error("BasicQueueProcessor:runOnUiThread", e);
				}
			}
		});
	}

	@Override
	protected void finish() {
	}
}

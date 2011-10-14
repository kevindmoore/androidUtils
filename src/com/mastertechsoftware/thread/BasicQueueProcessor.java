package com.mastertechsoftware.thread;

/**
 * User: kevin.moore
 */
public class BasicQueueProcessor<QueueItem> extends QueueProcessor<BasicQueueTask<QueueItem>> {

	@Override
	protected void process(final BasicQueueTask<QueueItem> queueTask) {
//		runOnce = true;
		queueTask.process();
		queueTask.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				queueTask.finished();
			}
		});
	}

	@Override
	protected void finish() {
	}
}

package com.mastertechsoftware.thread;

/**
 * User: kevin.moore
 */
public abstract class AbstractQueueTask<QueueItem> implements BasicQueueTask<QueueItem> {

	@Override
	public void process(QueueItem queueItem) {

	}

	@Override
	public void finished() {

	}
}

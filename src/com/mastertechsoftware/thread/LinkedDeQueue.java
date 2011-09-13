package com.mastertechsoftware.thread;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * User: kevin.moore
 * Since the LinkedBlockingDeque is in 2.3, Make a wrapper class that works in 2.2
 */
public class LinkedDeQueue<QueueItem> extends LinkedQueue<QueueItem> {
	protected String TAG = "LinkedDeQueue";

	/**
	 * Create a LinkedBlocking Queue
	 */
	public void createQueue() {
		queue = new LinkedBlockingDeque<QueueItem>();
	}

	/**
	 * Add a queue item to the head of the list
	 * @param queueItem
	 */
	@Override
	public void addFirst(QueueItem queueItem) {
		((LinkedBlockingDeque)queue).addFirst(queueItem);
	}
}

package com.mastertechsoftware.thread;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * User: kevin.moore
 * Since the LinkedBlockingDeque is in 2.3, Make a wrapper class that works in 2.2
 */
public class LinkedDeQueue<QueueItem> extends LinkedQueue<QueueItem> {

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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addFirst(QueueItem queueItem) {
		((LinkedBlockingDeque)queue).addFirst(queueItem);
	}
}

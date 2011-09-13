package com.mastertechsoftware.thread;


import android.util.Log;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * User: kevin.moore
 * Since the LinkedBlockingDeque is in 2.3, Make a wrapper class that works in 2.2
 */
public class LinkedQueue<QueueItem> {
	protected String TAG = "LinkedQueue";
	protected BlockingQueue<QueueItem> queue;
    private static final int POLL_TIMEOUT = 1000; // Milliseconds


	/**
	 * Create a LinkedBlocking Queue
	 */
	public void createQueue() {
		queue = new LinkedBlockingQueue<QueueItem>();
	}

	/**
	 * Add queue item to list
	 * @param queueItem
	 */
	public void addQueue(QueueItem queueItem) {
		queue.add(queueItem);
	}

	/**
	 * Add a queue item to the head of the list
	 * @param queueItem
	 */
	public void addFirst(QueueItem queueItem) {
        LinkedBlockingQueue<QueueItem> copyQueue = new LinkedBlockingQueue<QueueItem>(queue);
        queue.clear();
		queue.add(queueItem);
        queue.addAll(copyQueue);
	}

    /**
     * Return an iterator for this queue
     * @return Iterator<QueueItem>
     */
    public Iterator<QueueItem> iterator() {
      return queue.iterator();
    }

	/**
	 * Take the top item from the queue
	 * @return QueueItem
	 */
	public QueueItem take() {
		try {
			return queue.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Log.e(TAG, "LinkedQueue:InterruptedException. Problems calling take. " + e.getMessage());
		}
		return null;
	}

    /**
     * Return the current size of the queue.
     * @return size of the queue
     */
    public int size() {
        return queue.size();
    }

	/**
	 * Remove all items from the queue
	 */
	public void clear() {
		queue.clear();
	}
}

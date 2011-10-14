package com.mastertechsoftware.thread;


import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: kevin.moore
 */
public abstract class QueueProcessor<QueueItem> implements Runnable {
	protected String TAG = "QueueProcessor";
	protected boolean running = true;
	protected boolean shutdown = false;
	protected boolean runOnce = false;
	protected LinkedQueue<QueueItem> queue;


	/**
	 * Constructor that decides which queue to use based on version
	 */
	protected QueueProcessor() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			queue = new LinkedDeQueue<QueueItem>();
		} else {
			queue = new LinkedQueue<QueueItem>();
		}
		queue.createQueue();
	}

	/**
	 * Add queue item to list
	 * @param queueItem
	 */
	public void addQueue(QueueItem queueItem) {
		queue.addQueue(queueItem);
	}

	/**
	 * Add a queue item to the head of the list
	 * @param queueItem
	 */
	public void addFirst(QueueItem queueItem) {
		queue.addFirst(queueItem);
	}

    /**
     * See if this queue item already exists in the queue
     * @param queueItem
     * @return true if it already exists
     */
    public boolean contains(QueueItem queueItem) {
        Iterator<QueueItem> iterator = queue.iterator();
        while (iterator.hasNext()) {
            QueueItem nextQueueItem = iterator.next();
            if (nextQueueItem.equals(queueItem)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the current items on the queue
     * @return List<QueueItem>
     */
    public List<QueueItem> getCurrentQueueList() {
        List<QueueItem> queueList = new ArrayList<QueueItem>();
        Iterator<QueueItem> iterator = queue.iterator();
        while (iterator.hasNext()) {
            QueueItem nextQueueItem = iterator.next();
            queueList.add(nextQueueItem);
        }
        return queueList;
    }

	/**
	 * Are we currently running.
	 * @return true if running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Set the running state.
	 * @param running
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

    public boolean isRunOnce() {
        return runOnce;
    }

    public void setRunOnce(boolean runOnce) {
        this.runOnce = runOnce;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    @Override
	public void run() {
		startup();
		try {
			while (running) {
				QueueItem item = queue.take();
                if (item != null) {
				   process(item);
                }
                if (shutdown && queue.size() == 0) {
                    break;
                }
                if (runOnce) {
                    break;
                }
			}

		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		finish();
	}

	/**
	 * Override these methods
	 */
	protected void startup() {}

	protected abstract void process(QueueItem item);

	protected void finish() {}
}

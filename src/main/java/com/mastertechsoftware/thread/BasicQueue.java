package com.mastertechsoftware.thread;

/**
 * User: kevin.moore
 */
public class BasicQueue<QueueItem> {
	private BasicQueueTask<QueueItem> queueTask;
	private QueueItem data;

	public BasicQueue(QueueItem data, BasicQueueTask<QueueItem> queueTask) {
		this.data = data;
		this.queueTask = queueTask;
	}

	public QueueItem getData() {
		return data;
	}

	public BasicQueueTask<QueueItem> getQueueTask() {
		return queueTask;
	}
}

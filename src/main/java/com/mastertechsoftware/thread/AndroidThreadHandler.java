package com.mastertechsoftware.thread;

import android.os.Handler;

import com.mastertechsoftware.util.log.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
/**
 * Class to replace AsyncTask
 */
public class AndroidThreadHandler {
	protected final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
	protected AndroidThreadCallback callback;
	protected Handler handler = new Handler();
	protected LinkedBlockingDeque<AndroidThreadCallback> threadQueue = new LinkedBlockingDeque<AndroidThreadCallback>();

	/**
	 * Execute the runnable in the executor. Will run on another thread
	 *
	 * @return true if executed.
	 */
	public boolean executeTask(AndroidThreadCallback callback) {

		// Wrap the whole thing so we can make sure to unlock in
		// case something throws.
		try {

			// If we're shutdown or terminated we can't accept any new requests.
			if (mExecutor.isShutdown() || mExecutor.isTerminated()) {
				Logger.error("AndroidThreadHandler:executeTask - Executor is shutdown");
				return false;
			}

			// Push the request onto the queue.
			mExecutor.execute(new ThreadRunnable(callback));

		} catch (Exception RejectedExecutionException) {
			return false;
		}

		return true;
	}

	/**
	 * If we are not ready and want to queue up requests, add the callback here
	 * @param callback
	 */
	public void addCallbackToQueue(AndroidThreadCallback callback) {
		threadQueue.add(callback);
	}

	/**
	 * Execute all items on the queue and then empty it.
	 * @return false if any task fails
	 */
	public boolean executeQueue() {
		Logger.debug("executeQueue");
		boolean result = true;
		for (AndroidThreadCallback androidThreadCallback : threadQueue) {
			if (!executeTask(androidThreadCallback)) {
				result = false;
			}
		}
		threadQueue.clear();
		return result;
	}

	/**
	 * Shutdown this executor.
	 */
	public void shutdown() {
		// If we're shutdown or terminated we can't accept any new requests.
		if (!mExecutor.isShutdown() || !mExecutor.isTerminated()) {
			mExecutor.shutdown();
		}
	}

	/**
	 * Runnable that uses our callback and then runs the result on UI thread
	 */
	class ThreadRunnable implements Runnable {
		protected AndroidThreadCallback callback;

		ThreadRunnable(AndroidThreadCallback callback) {
			this.callback = callback;
		}

		@Override
		public void run() {
			try {
				final Object result = callback.doInBackground();
				handler.post(new Runnable() {
					@Override
					public void run() {
						try {
							callback.doOnMainThread(result);
						} catch (Exception e) {
							callback.handleError(e);
						}
					}
				});
			} catch (Exception e) {
				callback.handleError(e);
			}
		}
	}
}

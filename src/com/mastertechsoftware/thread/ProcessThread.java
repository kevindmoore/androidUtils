package com.mastertechsoftware.thread;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Kevin Moore
 */
public class ProcessThread<Index,ProcessObject> extends Thread {
	protected boolean continueProcessing = true;
	protected boolean running = false;
	protected boolean initialized = false;
	protected boolean waiting = false;
	protected boolean paused = false;
	protected boolean ownerFinished = false;
	protected ThreadWait threadWait = new ThreadWait();
	protected ThreadFinishedCallback callback;
	protected LinkedHashMap<Index, ProcessObject> processObjects = new LinkedHashMap<Index, ProcessObject>();
	protected ThreadProcessor<Index, ProcessObject> processor;

	public ProcessThread(ThreadProcessor<Index, ProcessObject> processor) {
		this.processor = processor;
	}
	
	public void addProcessObject(Index index, ProcessObject processObject) {
		synchronized (this) {
			if (!processObjects.containsKey(index)) {
				processObjects.put(index, processObject);
//				Logger.d("ImageLoadingThread:Really adding " + Index);
				if (waiting) {
					waiting = false;
					threadWait.resume();
				}
			}
		}
		
	}

	public int getProcessCount() {
		return processObjects.size();
	}

	public void setCallback(ThreadFinishedCallback callback) {
		this.callback = callback;
	}

	@Override
	public void run() {
		running = true;
		// Lower the priority of this thread to avoid competing with
		// the UI thread.
		setPriority(Thread.NORM_PRIORITY - 1);
		Map<Index, ProcessObject> currentlyProcessing = null;
		synchronized (this) {
			currentlyProcessing = new LinkedHashMap<Index, ProcessObject>(processObjects);
			processObjects.clear();
		}
		while (continueProcessing && currentlyProcessing.size() > 0) {
			checkPaused();
			for (Index index : currentlyProcessing.keySet()) {

				ProcessObject processObject = currentlyProcessing.get(index);
				if (!continueProcessing) {
					running = false;
					return;
				}
				checkPaused();
				processor.process(index, processObject);
			}

			// Check for new images loaded
			if (processObjects.size() > 0) {
				synchronized (this) {
					currentlyProcessing = new LinkedHashMap<Index, ProcessObject>(processObjects);
					processObjects.clear();
				}
			} else if (ownerFinished) {
				break;
			} else {
				currentlyProcessing.clear();
			}
			if (continueProcessing && currentlyProcessing.size() == 0) {
				waiting = true;
				paused = true;
				threadWait.pause();
				waiting = false;
				paused = false;
				if (processObjects.size() > 0) {
					synchronized (this) {
						currentlyProcessing = new LinkedHashMap<Index, ProcessObject>(processObjects);
						processObjects.clear();
					}
				}
			}
		}
		running = false;
		processor.finished();
		if (callback != null) {
			callback.threadFinished();
		}
	}

	public boolean isOwnerFinished() {
		return ownerFinished;
	}

	public void setOwnerFinished(boolean ownerFinished) {
		this.ownerFinished = ownerFinished;
	}

	public ThreadWait getThreadWait() {
		return threadWait;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	private void checkPaused() {
		if (paused) {
			threadWait.pause();
		}
	}

	/**
	 * Need to override so we don't get called multiple times
	 */
	@Override
	public void start() {
		initialized = true;
		super.start();
	}

	public void setContinueProcessing(boolean continueProcessing) {
		this.continueProcessing = continueProcessing;
		if (!continueProcessing && waiting) {
			waiting = false;
			threadWait.resume();
		}
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isInitialized() {
		return initialized;
	}
}

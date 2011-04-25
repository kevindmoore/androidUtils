package com.mastertechsoftware.thread;

/**
 * @author Kevin Moore
 */
public class ThreadWait {
	protected final Object waitObject = new Object();
	protected boolean waiting = false;

	public void pause() {
		waiting = true;
		while (waiting) {
			try {
				synchronized (waitObject) {
					waitObject.wait();
				}
			} catch (InterruptedException e) {

			}
		}
	}

	public void resume() {
		// Handle multiple calls
//		if (!waiting) {
//			return;
//		}
		waiting = false;
		synchronized (waitObject) {
			waitObject.notify();
		}
	}

	public  boolean isWaiting() {
		return waiting;
	}
}

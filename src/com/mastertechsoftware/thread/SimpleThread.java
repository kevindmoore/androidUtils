package com.mastertechsoftware.thread;


import com.mastertechsoftware.util.log.Logger;

/**
 * User: kevin.moore
 */
public abstract class SimpleThread extends Thread {
	protected boolean running = true;

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public void run() {
		startup();
		try {
			while (running) {
				process();
			}

		} catch (Exception e) {
			Logger.error(this, e.getMessage(), e);
		}
		finish();
	}

	/**
	 * Method to be called in while loop
	 */
	protected abstract void process();

	/**
	 * After ending run method, do any last tasks.
	 */
	protected void finish() {

	}

	/**
	 * Do any startup tasks before running process
	 */
	protected void startup() {

	}
}

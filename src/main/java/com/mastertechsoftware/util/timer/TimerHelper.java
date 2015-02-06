package com.mastertechsoftware.util.timer;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;
/**
 * Timer Helper for handling repeating timer
 */
public class TimerHelper extends TimerTask {
	private boolean paused = false;
	private boolean scheduled = false;
	private Timer timer;
	protected Handler handler;

	public TimerHelper(Handler handler) {
		this.handler = handler;
		timer = new Timer();
	}

	@Override
	public void run() {
		if (!paused) {
			handler.sendMessage(handler.obtainMessage());
		}
	}

	public void schedule(int delay, int period) {
		if (scheduled) {
			cancel();
		}
		scheduled = true;
		timer.schedule(this, delay, period);
	}

	public void schedule(int period) {
		if (scheduled) {
			cancel();
		}
		scheduled = true;
		timer.schedule(this, period);
	}

	public boolean cancel() {
		scheduled = false;
		timer.cancel();
		timer = new Timer();
		return super.cancel();
	}

	public boolean isPaused() {
		return paused;
	}
	public void setPaused(boolean paused) {
		this.paused = paused;
	}

}

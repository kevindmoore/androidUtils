package com.mastertechsoftware.util.timer;

import android.os.Handler;

import com.mastertechsoftware.util.log.Logger;

import java.util.Timer;
import java.util.TimerTask;
/**
 * Timer Helper for handling repeating timer
 */
public class TimerHelper {
	protected boolean paused = false;
	protected boolean scheduled = false;
	protected boolean oneTime = false;
	protected boolean cancelled = false;
	protected Timer timer;
	protected Handler handler;
	protected InternalTimerTask timerTask;
	protected int delay = -1;
	protected int period = -1;

	public TimerHelper(Handler handler) {
		this.handler = handler;
		timer = new Timer();
		timerTask = new InternalTimerTask();
	}


	public void schedule(int delay, int period) {
		if (isScheduled()) {
			cancel();
		}
		cancelled = false;
		scheduled = true;
		this.delay = delay;
		this.period = period;
		try {
			timer.schedule(timerTask, delay, period);
		} catch (IllegalStateException e) {
			Logger.error("Problems scheduling timer ", e);
		}
	}

	public void schedule(int period) {
		if (isScheduled()) {
			cancel();
		}
		cancelled = false;
		scheduled = true;
		oneTime = true;
		Logger.debug("Scheduling Timer for " + period);
		try {
			timer.schedule(timerTask, period);
		} catch (IllegalStateException e) {
			Logger.error("Problems scheduling timer ", e);
		}
	}

	public boolean isScheduled() {
		if (timer != null && scheduled) {
			return true;
		}
		return scheduled;
	}

	public boolean cancel() {
		scheduled = false;
		if (cancelled) {
			return true;
		}
		Logger.debug("Cancelling Timer");
		timer.cancel();
		cancelled = true;
		timer = new Timer();
		final boolean result = timerTask.cancel();
		timerTask = new InternalTimerTask();
		return result;
	}

	public boolean isPaused() {
		return paused;
	}
	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	/**
	 * Cancel & reschedule
	 */
	public void reset() {
		if (delay != -1) {
			schedule(delay, period);
		} else if (period != -1) {
			schedule(period);
		}
	}

	class InternalTimerTask extends TimerTask {

		@Override
		public void run() {
			if (!paused) {
				Logger.debug("TimerHelper:run");
				handler.sendMessage(handler.obtainMessage());
				if (oneTime) {
					cancel();
				}
			}
		}
	}
}

package com.mastertechsoftware.util.timer;

/**
 * @author Kevin Moore
 */
public class ProgressTimer {
	public static final int MILLISECONDS = 1000;
	public static final int SECONDS = MILLISECONDS * 1;
	public static final int MINUTES = SECONDS*60;
	public static final int HOURS = MINUTES*60;
	public static final int DAYS = HOURS*24;
	public static final int WEEKS = DAYS*5;
	public static final int MONTHS = WEEKS*4;
	public static final int YEARS = MONTHS*12;
	long start;
	long end;
	long mark;
	protected long hours;
	protected long minutes;
	protected long seconds;
	protected long milliSeconds;

	public void start() {
		start = System.currentTimeMillis();
	}

	public void stop() {
		end = System.currentTimeMillis();
        calcTime();
	}

	public void mark() {
		mark = System.currentTimeMillis();
	}

	public long getTime() {
		return (end-start);
	}

	public long getMark() {
		return (mark-start);
	}

	public void calcTime() {
		long totalMs = end - start;
		hours = totalMs/HOURS;
		minutes = totalMs/MINUTES;
		if (hours > 0 & minutes > 0) {
			minutes = minutes - (hours*60);
		}
		seconds = totalMs/SECONDS;
		if (minutes > 0 && seconds > 0) {
			seconds = seconds - (minutes*60);
		}
		milliSeconds = totalMs;
		if (seconds > 0 && milliSeconds > 0) {
			milliSeconds = milliSeconds - (seconds);
		}
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public long getHours() {
		return hours;
	}

	public long getMinutes() {
		return minutes;
	}

	public long getSeconds() {
		return seconds;
	}

	private long getMilliseconds() {
		return milliSeconds;
	}

	@Override
    public String toString() {
        return "Total time: Hours: " + getHours() + " Minutes: " + getMinutes() + " Seconds: " + getSeconds();
    }
    public String getResults() {
        return "Total time: Hours: " + getHours() + " Minutes: " + getMinutes() + " Seconds: " + getSeconds() + " Micro: " + getMilliseconds();
    }

}

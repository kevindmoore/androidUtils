package com.mastertechsoftware.util;

import android.util.Log;
import com.mastertechsoftware.util.log.SDLogger;

public class Logger {
	private static String applicationTag;

	public static void setApplicationTag(String applicationTag) {
		Logger.applicationTag = applicationTag;
	}

	public static void error(String tag, String message, Throwable exception) {
		if (applicationTag != null) {
			tag = applicationTag;
		} else	if (tag == null || tag.length() == 0) {
			tag = "";
		}
		if (message == null || message.length() == 0) {
			message = "";
		}
		Log.e(tag, message, exception);
        SDLogger.error(message, exception);
	}

	public static void error(String message) {
		if (message == null || message.length() == 0) {
			message = "";
		}
		Log.e(applicationTag, message);
        SDLogger.error(message, null);
	}

	public static void error(String message, Throwable exception) {
		if (message == null || message.length() == 0) {
			message = "";
		}
		Log.e(applicationTag, message, exception);
        SDLogger.error(message, exception);
	}

	public static void error(Throwable exception) {
		String message = StackTraceOutput.getFirstLineStackTrace(exception);
		if (message == null || message.length() == 0) {
			message = "";
		}
		Log.e(applicationTag, message, exception);
        SDLogger.error(message, exception);
	}

	public static void debug(String message) {
		if (message == null || message.length() == 0) {
			message = "";
		}
		Log.d(applicationTag, message);
	}

	public static void debug(String tag, String message) {
		if (applicationTag != null) {
			tag = applicationTag;
		} else	if (tag == null || tag.length() == 0) {
			tag = "";
		}
		if (message == null || message.length() == 0) {
			message = "";
		}
		Log.d(tag, message);
	}

    public static void debug(String tag, Throwable exception) {
		if (applicationTag != null) {
			tag = applicationTag;
		} else	if (tag == null || tag.length() == 0) {
			tag = "";
		}
        String message = StackTraceOutput.getStackTrace(exception);
        if (message == null || message.length() == 0) {
            message = "";
        }
        Log.d(tag, message);
    }


	public static void printTime(String tag, String message, long start, long end) {
		if (applicationTag != null) {
			tag = applicationTag;
		} else	if (tag == null || tag.length() == 0) {
			tag = "";
		}
		if (message == null || message.length() == 0) {
			message = "";
		}
        long seconds = (end-start)/1000;
        long minutes = seconds/60;
        seconds = seconds - (minutes*60);
        long hours = minutes/60;
        minutes = minutes - (hours*60);

		Log.d(tag, message + " Hours: " + hours + " Minutes: " + minutes + " Seconds: " + seconds);
	}
}

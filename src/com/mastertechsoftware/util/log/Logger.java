package com.mastertechsoftware.util.log;

import android.util.Log;

import com.mastertechsoftware.util.StackTraceOutput;

import java.util.HashMap;

/**
 * Class used to funnel all error messages so that we can log to sd card.
 */
public class Logger {

    private static String applicationTag = "MasterTech";
    private static HashMap<String, Boolean> classesDebugStates = new HashMap<String, Boolean>();

    /**
     * Required. Set the tag that will always show. Usually your application name.
     * @param applicationTag
     */
    public static void setApplicationTag(String applicationTag) {
        Logger.applicationTag = applicationTag;
    }

    /**
     * Log an errog with a tag. Application tag will override.
     * @param tag
     * @param message
     * @param exception
     */
    public static void error(String tag, String message, Throwable exception) {
        if (applicationTag != null) {
            tag = applicationTag;
        } else if (tag == null || tag.length() == 0) {
            tag = "";
        }
        if (message == null || message.length() == 0) {
            message = "";
        }
        if (exception != null) {
            String msg = buildErrorString(exception) + message;
            Log.e(tag, msg, exception);
            SDLogger.error(msg, exception);
        } else {
            Log.e(applicationTag, message);
            SDLogger.error(message, null);
        }
    }

    public static void error(String message) {
        error(applicationTag, message, null);
    }

    public static void error(String message, Throwable exception) {
        error(applicationTag, message, exception);
    }

    public static void error(Throwable exception) {
        String message = StackTraceOutput.getFirstLineStackTrace(exception);
        error(applicationTag, message, exception);
    }

    public static void error(Throwable exception, int level) {
        String message = StackTraceOutput.getFirstLineStackTrace(exception);
        if (message == null || message.length() == 0) {
            message = "";
        }
        String msg = buildErrorString(exception) + message;
        Log.e(applicationTag, msg, exception);
        SDLogger.error(msg, exception, level);
    }


    public static void error(Object caller, String message) {
        error(caller, message, null);
    }

    /**
     * Log an error message
     */
    public static void error(Object caller, String message, Throwable exception) {
        if (message == null || message.length() == 0) {
            message = "";
        }
        if (exception != null) {
            Log.e(applicationTag, caller.getClass().getSimpleName() + ": " + buildErrorString(exception) + message, exception);
        } else {
            Log.e(applicationTag, caller.getClass().getSimpleName() + ": " + message);
        }
    }

    protected static String buildErrorString(Throwable exception) {
        StringBuilder builder = new StringBuilder();
        Throwable cause = exception.getCause();
        builder.append(exception.toString()).append(": ");
        if (cause != null) {
            builder.append("\nCaused by: ").append(cause).append("\n");
        }
        builder.append("\n");
        return builder.toString();
    }

    public static void setDebug(String classToDebug, Boolean enabled) {
        classesDebugStates.put(classToDebug, enabled);
    }

    /**
     * Log a debug message.
     */
    public static void debug(Object caller, String message) {
        String simpleName = caller.getClass().getSimpleName();
		debugLocal(simpleName, message);
    }

	public static void debugLocal(String simpleName, String message) {
		Boolean debugEnabled = classesDebugStates.get(simpleName);
		if (debugEnabled != null && !debugEnabled) {
			return;
		}
		if (message == null || message.length() == 0) {
			message = "";
		}
		if (simpleName != null && simpleName.length() > 1) {
			message = simpleName + ": " + message;
		}

		Log.d(applicationTag, message);
	}

    public static void debug(String message) {
        debug(applicationTag, message);
    }

    public static void debugNow(String message) {
        if (message == null || message.length() == 0) {
            message = "";
        }
        Log.d(applicationTag, message);
    }

	/**
	 * Another debugNow call so we can just replace debug with debugNow(this
	 * @param caller
	 * @param message
	 */
    public static void debugNow(Object caller, String message) {
        if (message == null || message.length() == 0) {
            message = "";
        }
        Log.d(applicationTag, message);
    }

    public static void debug(String tag, String message) {
        if (applicationTag != null) {
            tag = applicationTag;
        } else if (tag == null || tag.length() == 0) {
            tag = "";
        }
        if (message == null || message.length() == 0) {
            message = "";
        }
        Log.d(tag, message);
    }

    public static void printTime(String tag, String message, long start, long end) {
        if (applicationTag != null) {
            tag = applicationTag;
        } else if (tag == null || tag.length() == 0) {
            tag = "";
        }
        if (message == null || message.length() == 0) {
            message = "";
        }
        long seconds = (end - start) / 1000;
        long minutes = seconds / 60;
        seconds = seconds - (minutes * 60);
        long hours = minutes / 60;
        minutes = minutes - (hours * 60);

        Log.d(tag, message + " Hours: " + hours + " Minutes: " + minutes + " Seconds: " + seconds);
    }
}

package com.mastertechsoftware.util.log;

import android.util.Log;

import com.mastertechsoftware.util.StackTraceOutput;

import java.util.HashMap;

/**
 * Class used to funnel all error messages so that we can log to sd card.
 */
public class Logger {
	public enum TYPE {
		INFO,
		VERBOSE,
		DEBUG,
		WARNING,
		ERROR,
		WTF
	}

    private static String applicationTag = "MasterTech";
    private static HashMap<String, Boolean> classesDebugStates = new HashMap<String, Boolean>();
    private static HashMap<String, Boolean> classesDebugLogging = new HashMap<String, Boolean>();
	private static boolean debuggingDisabled = false;

    /**
     * Required. Set the tag that will always show. Usually your application name.
     * @param applicationTag
     */
    public static void setApplicationTag(String applicationTag) {
        Logger.applicationTag = applicationTag;
    }

    /**
	 * Globally disable/enable debugging. Useful when you want to turn off debugging in release builds
	 * @param debuggingDisabled
	 */
	public static void setDebuggingDisabled(boolean debuggingDisabled) {
		Logger.debuggingDisabled = debuggingDisabled;
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

	/**
	 * Log An error but only show level # of lines in the file
	 * @param exception
	 * @param level
	 */
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
			String msg = caller.getClass().getSimpleName() + ": " + buildErrorString(exception) + message;
			Log.e(applicationTag, msg, exception);
			SDLogger.error(msg, exception);
        } else {
			String msg = caller.getClass().getSimpleName() + ": " + message;
			Log.e(applicationTag, msg);
			SDLogger.error(msg, exception);
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

    public static void setDebugLogging(String classToDebug, Boolean enabled) {
		classesDebugLogging.put(classToDebug, enabled);
    }

    /**
     * Log a debug message.
     */
    public static void debug(Object caller, String message) {
        String simpleName = caller.getClass().getSimpleName();
		debugLocal(TYPE.DEBUG, applicationTag, simpleName, message);
    }

    public static void debug(String tag, Object caller, String message) {
        String simpleName = caller.getClass().getSimpleName();
		debugLocal(TYPE.DEBUG, tag, simpleName, message);
    }

    public static void debug(Class caller, String message) {
        String simpleName = caller.getSimpleName();
		debugLocal(TYPE.DEBUG, applicationTag, simpleName, message);
    }

    public static void info(Object caller, String message) {
        String simpleName = caller.getClass().getSimpleName();
		debugLocal(TYPE.INFO, applicationTag, simpleName, message);
    }

    public static void warn(Object caller, String message) {
        String simpleName = caller.getClass().getSimpleName();
		debugLocal(TYPE.WARNING, applicationTag, simpleName, message);
    }

    public static void verbose(Object caller, String message) {
        String simpleName = caller.getClass().getSimpleName();
		debugLocal(TYPE.VERBOSE, applicationTag, simpleName, message);
    }

	public static void debugLocal(String simpleName, String message) {
		debugLocal(TYPE.DEBUG, applicationTag, simpleName, message);
	}

	public static void debugLocal(TYPE type, String tag, String simpleName, String message) {
		Boolean debugEnabled = classesDebugStates.get(simpleName);
		if (debuggingDisabled || debugEnabled != null && !debugEnabled) {
			return;
		}
		if (message == null || message.length() == 0) {
			message = "";
		}
		if (simpleName != null && simpleName.length() > 1) {
			message = simpleName + ": " + message;
		}
		switch (type) {
			case INFO:
				Log.i(tag, message);
				break;
			case VERBOSE:
				Log.v(tag, message);
				break;
			case WARNING:
				Log.w(tag, message);
				break;
			case DEBUG:
				Log.d(tag, message);
				break;
			case WTF:
				Log.wtf(tag, message);
				break;
		}
		Boolean logDebugMsgs = classesDebugLogging.get(simpleName);
		if (logDebugMsgs != null && logDebugMsgs) {
			SDLogger.log(message);
		}
	}


    public static void debug(String message) {
        debug(applicationTag, message);
    }

    public static void debugNow(String message) {
		if (debuggingDisabled) {
			return;
		}
        if (message == null || message.length() == 0) {
            message = "";
        }
        Log.d(applicationTag, message);
    }

	public static void wtf(Object caller, String message) {
		if (debuggingDisabled) {
			return;
		}
		String simpleName = caller.getClass().getSimpleName();
		debugLocal(TYPE.WTF, applicationTag, simpleName, message);
	}

	/**
	 * Another debugNow call so we can just replace debug with debugNow(this
	 * @param caller
	 * @param message
	 */
    public static void debugNow(Object caller, String message) {
		if (debuggingDisabled) {
			return;
		}
        if (message == null || message.length() == 0) {
            message = "";
        }
        Log.d(applicationTag, message);
    }

    public static void debug(String tag, String message) {
		if (debuggingDisabled) {
			return;
		}
        if (applicationTag != null) {
            tag = applicationTag;
        }

        if (tag == null || tag.length() == 0) {
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

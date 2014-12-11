package com.mastertechsoftware.util.log;

import android.text.TextUtils;
import android.util.Log;

import com.mastertechsoftware.util.StackTraceOutput;

import java.util.HashMap;

/**
 * Class used to funnel all error messages so that we can log to sd card.
 */
public class Logger {
    public static final int MESSAGE_MAX_LENGTH = 4000;

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
        try {
        if (exception != null) {
            String msg = buildErrorString(exception) + message;
                splitLongMessage(TYPE.ERROR, tag, msg, exception);
            SDLogger.error(msg, exception);
        } else {
                splitLongMessage(TYPE.ERROR, tag, message, null);
            SDLogger.error(message, null);
        }
        } catch (OutOfMemoryError oom) {
            Log.e(tag,"OOM Error caught in Logger.error(String tag, String message, Throwable exception)!", oom);
        } catch(Exception e) {
            Log.e(tag,"Error caught in Logger.error(String tag, String message, Throwable exception)!", e);
        }
    }

    /**
     * Check to see if we need to split up the message into smaller chunks as Android can only print out a string so long.
     * @param type
     * @param tag
     * @param message
     * @param throwable
     */
    protected static void splitLongMessage(TYPE type, String tag, String message, Throwable throwable) {
        int chunkCount = message.length() / MESSAGE_MAX_LENGTH;
        for (int i = 0; i <= chunkCount; i++) {
            int max = MESSAGE_MAX_LENGTH * (i+1);
            String substring;
            if (max >= message.length()) {
                substring = message.substring(MESSAGE_MAX_LENGTH * i);
            } else {
                substring = message.substring(MESSAGE_MAX_LENGTH * i, max);
            }

            if (throwable != null) {
                switch (type) {
                    case DEBUG:
                        Log.d(tag, substring, throwable);
                        break;
                    case ERROR:
                        Log.e(tag, substring, throwable);
                        break;
                    case INFO:
                        Log.i(tag, substring, throwable);
                        break;
                    case VERBOSE:
                        Log.v(tag, substring, throwable);
                        break;
                    case WARNING:
                        Log.w(tag, substring, throwable);
                        break;
                    case WTF:
                        Log.wtf(tag, substring, throwable);
                        break;
                }
            } else {
                switch (type) {
                    case DEBUG:
                        Log.d(tag, substring);
                        break;
                    case ERROR:
                        Log.e(tag, substring);
                        break;
                    case INFO:
                        Log.i(tag, substring);
                        break;
                    case VERBOSE:
                        Log.v(tag, substring);
                        break;
                    case WARNING:
                        Log.w(tag, substring);
                        break;
                    case WTF:
                        Log.wtf(tag, substring);
                        break;
                }
            }
        }
    }

    public static void error(String message) {
        error(applicationTag, message, null);
    }

	public static void error(String message, Object ... args) {
        error(applicationTag, parseMessage(message, args), null);
	}

    private static String parseMessage(String message, Object ... args) {
        StringBuilder builder = new StringBuilder();
        if (message.contains("{}")) {
            String[] strings = message.split("\\{\\}");
            int argCount = 0;
            int argLength = args.length;
            for (String string : strings) {
                builder.append(string);
                if (argCount < argLength) {
                    builder.append(args[argCount++]);
                }
            }
        }
        return builder.toString();
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
        try {
        String msg = buildErrorString(exception) + message;
            splitLongMessage(TYPE.ERROR, applicationTag, msg, exception);
        SDLogger.error(msg, exception, level);
        } catch (OutOfMemoryError oom) {
            Log.e(applicationTag,"OOM Error caught in Logger.error(Throwable exception, int level)!", oom);
        } catch(Exception e) {
            Log.e(applicationTag,"Error caught in Logger.error(Throwable exception, int level)!", e);
        }
    }


    public static void error(Object caller, String message) {
        error(caller, message, null);
    }

    public static void error(String tag, String message) {
        error(tag, message, null);
    }

    /**
     * Log an error message
     */
    public static void error(Object caller, String message, Throwable exception) {
        if (message == null || message.length() == 0) {
            message = "";
        }
        try {
        if (exception != null) {
                String msg = (caller instanceof Class ? ((Class) caller).getSimpleName() : caller.getClass().getSimpleName()) + ": " + buildErrorString(exception) + message;
                splitLongMessage(TYPE.ERROR, applicationTag, msg, exception);
			SDLogger.error(msg, exception);
        } else {
                String msg = (caller instanceof Class ? ((Class) caller).getSimpleName() : caller.getClass().getSimpleName()) + ": " + message;
                splitLongMessage(TYPE.ERROR, applicationTag, msg, null);
			SDLogger.error(msg, exception);
        }
        } catch (OutOfMemoryError oom) {
            Log.e(applicationTag,"OOM Error caught in Logger.error(Object caller, String message, Throwable exception)!", oom);
        } catch(Exception e) {
            Log.e(applicationTag,"Error caught in Logger.error(Object caller, String message, Throwable exception)!", e);
        }
    }

    /**
     * Build an error string from the given exception
     * @param exception
     * @return error string
     */
    protected static String buildErrorString(Throwable exception) {
        StringBuilder builder = new StringBuilder();
        Throwable cause = exception.getCause();
        try {
        builder.append(exception.toString()).append(": ");
        if (cause != null) {
            builder.append("\nCaused by: ").append(cause).append("\n");
        }
        builder.append("\n");
        return builder.toString();
        } catch (OutOfMemoryError oom) {
            Log.e(applicationTag,"OOM Error caught in Logger.BuildErrorString(Throwable exception)!", oom);
            return "Unable to build error string! ";
        } catch (Exception e) {
             Log.e(applicationTag,"Error caught in Logger.BuildErrorString(Throwable exception)!", e);
            return "Unable to build error string! ";
        }
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
        String simpleName = caller instanceof Class ? ((Class) caller).getSimpleName() : caller.getClass().getSimpleName();
		debugLocal(TYPE.DEBUG, applicationTag, simpleName, message);
    }

    public static void debug(String tag, String message, Throwable throwable) {
        debugLocal(TYPE.DEBUG, applicationTag, tag, message, throwable);
    }

    public static void debug(String tag, Object caller, String message) {
        String simpleName = caller instanceof Class ? ((Class) caller).getSimpleName() : caller.getClass().getSimpleName();
		debugLocal(TYPE.DEBUG, tag, simpleName, message);
    }

    public static void debug(Class caller, String message) {
        String simpleName = caller instanceof Class ? ((Class) caller).getSimpleName() : caller.getClass().getSimpleName();
		debugLocal(TYPE.DEBUG, applicationTag, simpleName, message);
    }

    public static void info(Object caller, String message) {
        String simpleName = caller instanceof Class ? ((Class) caller).getSimpleName() : caller.getClass().getSimpleName();
		debugLocal(TYPE.INFO, applicationTag, simpleName, message);
    }

    public static void info(String tag, String message) {
        debugLocal(TYPE.INFO, applicationTag, tag, message);
    }

	public static void info(String message) {
		debugLocal(TYPE.INFO, applicationTag, null, message);
	}

	public static void info(String message, Object ... args) {
        info(applicationTag, parseMessage(message, args));
	}

	public static void warn(String message) {
        debugLocal(TYPE.WARNING, applicationTag, null, message);
    }

	public static void warn(String message, Object ... args) {
        warn(applicationTag, parseMessage(message, args));
	}

	public static void warn(Object caller, String message) {
        String simpleName = caller instanceof Class ? ((Class) caller).getSimpleName() : caller.getClass().getSimpleName();
		debugLocal(TYPE.WARNING, applicationTag, simpleName, message);
    }

    public static void warn(Object caller, String message, Throwable t) {
        String simpleName = caller instanceof Class ? ((Class) caller).getSimpleName() : caller.getClass().getSimpleName();
		debugLocal(TYPE.WARNING, applicationTag, simpleName, message, t);
    }

    public static void warn(String tag, String message) {
        debugLocal(TYPE.WARNING, applicationTag, tag, message);
    }

    public static void verbose(String message) {
        debugLocal(TYPE.VERBOSE, applicationTag, null, message);
    }

    public static void verbose(Object caller, String message) {
        String simpleName = caller instanceof Class ? ((Class) caller).getSimpleName() : caller.getClass().getSimpleName();
		debugLocal(TYPE.VERBOSE, applicationTag, simpleName, message);
    }

    public static void verbose(String tag, String message) {
        debugLocal(TYPE.VERBOSE, applicationTag, tag, message);
	}

	public static void debugLocal(TYPE type, String tag, String simpleName, String message) {
        debugLocal(type, tag, simpleName, message, null);
    }

	public static void debugLocal(TYPE type, String tag, String simpleName, String message, Throwable throwable) {
		Boolean debugEnabled = classesDebugStates.get(simpleName);
		if (debuggingDisabled || (debugEnabled != null && !debugEnabled)) {
			return;
		}
		if (message == null || message.length() == 0) {
			message = "";
		}
        try {
		if (simpleName != null && simpleName.length() > 1) {
			message = simpleName + ": " + message;
		}
            splitLongMessage(type, tag, message, throwable);
        } catch (OutOfMemoryError oom) {
            Log.e(applicationTag,"OOM Error caught in Logger.debugLocal(TYPE type, String tag, String simpleName, String message, Throwable throwable)! switch case was: " + type, oom);
        } catch (Exception e) {
            Log.e(applicationTag,"Error caught in Logger.debugLocal(TYPE type, String tag, String simpleName, String message, Throwable throwable)! switch case was: " + type, e);
		}

        try {
			Boolean logDebugMsgs = classesDebugLogging.get(simpleName);
			if (logDebugMsgs != null && logDebugMsgs) {
				SDLogger.log(message);
			}
        } catch (OutOfMemoryError oom) {
            Log.e(applicationTag,"OOM Error caught in Logger.debugLocal(TYPE type, String tag, String simpleName, String message, Throwable throwable) while attempting SDLogger!", oom);
        } catch (Exception e) {
            Log.e(applicationTag,"Error caught in Logger.debugLocal(TYPE type, String tag, String simpleName, String message, Throwable throwable) while attempting SDLogger!", e);
        }
	}


    public static void debug(String message) {
        debug(applicationTag, message);
    }

	public static void debug(String message, Object... args) {
        debug(applicationTag, parseMessage(message, args));
	}
    public static void debugNow(String message) {
		if (debuggingDisabled) {
			return;
		}
        if (message == null || message.length() == 0) {
            message = "";
        }
        try {
            splitLongMessage(TYPE.DEBUG, applicationTag, message, null);
        } catch (OutOfMemoryError oom) {
            Log.e(applicationTag,"OOM Error caught in Logger.debugNow(String message)!", oom);
        } catch (Exception e) {
            Log.e(applicationTag,"Error caught in Logger.debugNow(String message)!", e);
        }
    }

	public static void wtf(Object caller, String message) {
		if (debuggingDisabled) {
			return;
		}
        String simpleName = caller instanceof Class ? ((Class) caller).getSimpleName() : caller.getClass().getSimpleName();
		debugLocal(TYPE.WTF, applicationTag, simpleName, message);
	}

    public static void wtf(String tag, String message) {
        if (debuggingDisabled) {
            return;
        }
        debugLocal(TYPE.WTF, applicationTag, tag, message);
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
        try {
            splitLongMessage(TYPE.DEBUG, applicationTag, message, null);
        } catch (OutOfMemoryError oom) {
            Log.e(applicationTag, "OOM Error caught in Logger.debugNow(Object caller, String message)!", oom);
        } catch (Exception e) {
            Log.e(applicationTag, "Error caught in Logger.debugNow(Object caller, String message)!", e);
        }
    }

    public static void debug(String tag, String message) {
		if (debuggingDisabled) {
			return;
		}
		String callerClassName = StackTraceOutput.getCallerClassName();
		Boolean debugEnabled = classesDebugStates.get(callerClassName);
		if (debuggingDisabled || (debugEnabled != null && !debugEnabled)) {
			return;
		}
        if (TextUtils.isEmpty(tag)) {
            tag = applicationTag;
        }

        if (TextUtils.isEmpty(tag)) {
            tag = "";
        }
        if (message == null || message.length() == 0) {
            message = "";
        }
        try {
            splitLongMessage(TYPE.DEBUG, tag, message, null);
        }
        catch (OutOfMemoryError oom) {
            Log.e(applicationTag, "OOM Error caught in Logger.debug(String tag, String message)!", oom);
        }
        catch (Exception e) {
            Log.e(applicationTag, "Error caught in Logger.debug(String tag, String message)!", e);
        }
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

        try {
        Log.d(tag, message + " Hours: " + hours + " Minutes: " + minutes + " Seconds: " + seconds);
        } catch (OutOfMemoryError oom) {
            Log.e(applicationTag, "OOM Error caught in Logger.printTime(String tag, String message, long start, long end)!", oom);
        } catch(Exception e) {
            Log.e(applicationTag, "Error caught in Logger.printTime(String tag, String message, long start, long end)!", e);
        }
    }
}

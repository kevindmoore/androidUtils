package com.mastertechsoftware.util.log;

import android.util.Log;

import com.mastertechsoftware.util.StackTraceOutput;

public class Logger {

    private static String applicationTag;

    public static void setApplicationTag(String applicationTag) {
        Logger.applicationTag = applicationTag;
    }

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
            Log.e(tag, buildErrorString(exception) + message, exception);
            SDLogger.error(buildErrorString(exception) + message, exception);
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
        Log.e(applicationTag, buildErrorString(exception) + message, exception);
        SDLogger.error(buildErrorString(exception) + message, exception, level);
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
        builder.append(exception.toString()).append(": ").append("\nCaused by: ").append(exception.getCause()).append("\n");
        return builder.toString();
    }

    /**
     * Log a debug message.
     */
    public static void debug(Object caller, String message) {
        if (message == null || message.length() == 0) {
            message = "";
        }
        Log.d(applicationTag, caller.getClass().getSimpleName() + ": " + message);
    }

    public static void debug(String message) {
        debug(applicationTag, message);
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

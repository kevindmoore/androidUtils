package com.mastertechsoftware.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;
import android.util.Log;

import com.mastertechsoftware.activity.CurrentActivityListener;
import com.mastertechsoftware.util.log.SDLogger;

import java.lang.Thread.UncaughtExceptionHandler;

public class ExceptionHandler implements UncaughtExceptionHandler {
	protected Thread.UncaughtExceptionHandler previousUncaughtExceptionHandler;
	protected Thread mUiThread;
	final Handler mHandler = new Handler();
    protected CurrentActivityListener currentActivityListener;

	public ExceptionHandler(CurrentActivityListener currentActivityListener, Thread.UncaughtExceptionHandler previousUncaughtExceptionHandler) {
        this.currentActivityListener = currentActivityListener;
		this.previousUncaughtExceptionHandler = previousUncaughtExceptionHandler;
		mUiThread = Thread.currentThread();
	}

	@Override
	public void uncaughtException(Thread thread, final Throwable error) {
        SDLogger.error("ExceptionHandler:uncaughtException", error);
		Log.e("", "Exception:  " + error.getMessage());
		Log.e("", "StackTrace:  " + StackTraceOutput.getStackTrace(error));
        if (currentActivityListener != null) {
            // Dialogs seem to only use Activity context, not Application Contexts
            final Activity currentActivity = currentActivityListener.getCurrentActivity();
            if (currentActivity != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
                            builder
                                .setTitle("Exception!")
                                .setMessage(error.toString())
                                .setPositiveButton("OK", null)
                                .show();
                        } catch (Exception e) {
                            Log.e("", "Problems showing Dialog", e);
                        }
                    }
                });

            }
        }
	}

}

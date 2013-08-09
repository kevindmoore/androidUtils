package com.mastertechsoftware.util;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.mastertechsoftware.util.log.SDLogger;

import java.lang.Thread.UncaughtExceptionHandler;

public class ExceptionHandler implements UncaughtExceptionHandler {
	protected Thread.UncaughtExceptionHandler previousUncaughtExceptionHandler;
	protected Context applicationContext;
	protected View dialog;
	protected Thread mUiThread;
	final Handler mHandler = new Handler();

	public ExceptionHandler(Context context, Thread.UncaughtExceptionHandler previousUncaughtExceptionHandler) {
		if (context instanceof Application) {
			this.applicationContext = context;
		} else {
			this.applicationContext = context.getApplicationContext();
		}
		this.previousUncaughtExceptionHandler = previousUncaughtExceptionHandler;
		mUiThread = Thread.currentThread();
	}

	@Override
	public void uncaughtException(Thread thread, final Throwable error) {
        SDLogger.error("ExceptionHandler:uncaughtException", error);
		Log.e("", "Exception:  " + error.getMessage());
		Log.e("", "StackTrace:  " + StackTraceOutput.getStackTrace(error));
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					AlertDialog.Builder builder = new AlertDialog.Builder(applicationContext);
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

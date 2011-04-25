package com.mastertechsoftware.util;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.mastertechsoftware.util.log.SDLogger;

import java.lang.Thread.UncaughtExceptionHandler;

public class ExceptionHandler implements UncaughtExceptionHandler {
	private Handler handler;
	private Context context;

	public ExceptionHandler(Context context) {
		this.context = context;
	}

	@Override
	public void uncaughtException(Thread thread, final Throwable error) {
        SDLogger.error("ExceptionHandler:uncaughtException", error);
		Log.e("", "Exception:  " + error.getMessage());
		Log.e("", "StackTrace:  " + StackTraceOutput.getStackTrace(error));
		if (handler == null) {
			handler = new Handler(context.getMainLooper());
		}
		handler.post(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder
					.setTitle("Exception!")
					.setMessage(error.toString())
					.setPositiveButton("OK", null)
					.show();
			}
		});
	}

}

package com.mastertechsoftware.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * @author Kevin Moore
 */
public class LooperThread extends HandlerThread {
	protected Handler handler;
	protected LooperCallback callback;

	public LooperThread(LooperCallback callback) {
		super("LooperThread");
		this.callback = callback;
	}

	@Override
	protected void onLooperPrepared() {
		handler = new SubHandler(getLooper(), callback);
		callback.setHandler(handler);
	}

	public Handler getHandler() {
		return handler;
	}

	class SubHandler extends Handler {
		SubHandler() {
		}

		SubHandler(Callback callback) {
			super(callback);
		}

		SubHandler(Looper looper) {
			super(looper);
		}

		SubHandler(Looper looper, Callback callback) {
			super(looper, callback);
		}

		@Override
		public void handleMessage(Message msg) {
			callback.handleMessage(msg);
		}
	}

	public interface LooperCallback extends Handler.Callback {
		public void setHandler(Handler handler);
	}
}

package com.mastertechsoftware.util;

import android.os.AsyncTask;

public abstract class AbstractAsync extends AsyncTask<Void, Integer, Void> {
	protected ParamHolder holder = new ParamHolder();
	protected AsyncCallback callback;
	
	public AbstractAsync() {
	}
	
	public AbstractAsync(AsyncCallback callback) {
		this.callback = callback;
	}

	public AbstractAsync(ParamHolder holder, AsyncCallback callback) {
		this.callback = callback;
		this.holder = holder;
	}
	
	public ParamHolder getHolder() {
		return holder;
	}

	@Override
	protected Void doInBackground(Void... params) {
		doBackground();
		return null;
	}

	public AsyncCallback getCallback() {
		return callback;
	}

	public void setCallback(AsyncCallback callback) {
		this.callback = callback;
	}

	public void setHolder(ParamHolder holder) {
		this.holder = holder;
	}

	@Override
	protected void onPostExecute(Void result) {
		finished();
		if (callback != null) {
			callback.asyncFinished(holder);
		}
	}

	abstract protected void doBackground();
	
	protected void finished() {
		
	}
}

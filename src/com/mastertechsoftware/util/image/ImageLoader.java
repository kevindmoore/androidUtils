package com.mastertechsoftware.util.image;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.mastertechsoftware.util.AsyncCallback;

public class ImageLoader extends AsyncTask<String, Void, Drawable> {

	private boolean isSearching = false;
	private AsyncCallback callback;

	public ImageLoader(AsyncCallback callback) {
		super();
		this.callback = callback;
	}
	
	public void setCallback(AsyncCallback callback) {
		this.callback = callback;
	}

	@Override
	protected Drawable doInBackground(String... params) {
		isSearching = true;
		return ImageUtil.ImageOperations(params[0]);
	}

	@Override
	protected void onPostExecute(Drawable result) {
		isSearching = false;
		if (callback != null) {
			callback.asyncFinished(result);
		}
	}

	
	public boolean isSearching() {
		return isSearching;
	}

}

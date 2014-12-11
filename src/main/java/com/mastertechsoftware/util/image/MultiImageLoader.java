package com.mastertechsoftware.util.image;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.mastertechsoftware.util.MultiAsyncCallback;

public class MultiImageLoader extends AsyncTask<Void, Void, Void> {
	private boolean isSearching = false;
	private MultiAsyncCallback callback;
	private List<String> urls;
	private List<Integer> positions;
	private int page;
	private boolean continueProcessing = true;

	public MultiImageLoader(int page, List<String> urls, List<Integer> positions, MultiAsyncCallback callback) {
		super();
		this.page = page;
		this.callback = callback;
		this.urls = urls;
		this.positions = positions;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		isSearching = true;
		int size = urls.size();
		for (int i=0; i < size && continueProcessing; i++) {
			Drawable drawable = ImageUtil.ImageOperations(urls.get(i));
			if (callback != null) {
				Integer position = 0;
				if (positions != null && positions.size() > i) {
					position = positions.get(i);
				}
				callback.processData(this, drawable, page, position);
			}
		}
		return (Void)null;
	}

	@Override
	protected void onPostExecute(Void result) {
		isSearching = false;
		if (callback != null) {
			callback.asyncFinished();
		}
	}
	
	public boolean isSearching() {
		return isSearching;
	}


	public int getPage() {
		return page;
	}

	public boolean isContinueProcessing() {
		return continueProcessing;
	}
	

	public void setContinueProcessing(boolean continueProcessing) {
		this.continueProcessing = continueProcessing;
	}


}

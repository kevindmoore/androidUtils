package com.mastertechsoftware.util;

import com.mastertechsoftware.util.image.MultiImageLoader;

public interface MultiAsyncCallback {
	void processData(MultiImageLoader loader, Object data, int page, int position);
	void asyncFinished();
}

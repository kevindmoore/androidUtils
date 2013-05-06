package com.mastertechsoftware.media;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.mastertechsoftware.util.log.Logger;
/**
 * Media Utilities
 */
public class MediaHelper {

	public static String getPath(Activity ctx, Uri uri) {
		if (uri == null) {
			Logger.error(MediaHelper.class, "getPath. Uri is null");
			return null;
		}
		String[] projection = {MediaStore.Images.Media.DATA};
		Cursor cursor = ctx.managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			if (cursor.moveToFirst() && column_index < cursor.getColumnCount()) {
				return cursor.getString(column_index);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}

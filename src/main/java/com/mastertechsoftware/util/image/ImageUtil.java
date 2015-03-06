package com.mastertechsoftware.util.image;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.mastertechsoftware.util.log.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ImageUtil {

	public static final int JPG_QUALITY = 92;
	protected static int defaultBufferSize = 8192;
	private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private static final String CACHE_DIR = "fileCache";
	public static String ALBUM_CACHE = "albumCache";
	public static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
	private static HashMap<String, Drawable> cache = new HashMap<String, Drawable>();
	private static HashMap<String, Bitmap> bitmapCache = new HashMap<String, Bitmap>();
	private static HashMap<String, HashMap<String, Bitmap>> caches = new HashMap<String, HashMap<String, Bitmap>>();

	static {
		// for the cache,
		// 565 is faster to decode and display
		// and we don't want to dither here because the image will be scaled down later

		sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		sBitmapOptions.inDither = false;
	}

	public static Bitmap getRemoteImage(final URL aURL) {
		try {
			final URLConnection conn = aURL.openConnection();
			conn.connect();
			final BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), defaultBufferSize);
			final Bitmap bm = BitmapFactory.decodeStream(bis);
			bis.close();
			return bm;
		} catch (OutOfMemoryError error) {
			Logger.error("Out of Memory Error for url " + aURL, error);
		} catch (IOException e) {
			Log.d("ImageUtil", "Problems loading Bitmap " + e.getMessage());
		}
		return null;
	}

	public static Bitmap getRemoteImageWithCookie(final String url, String cookie) {
		try {
			URL aURL = new URL(url);
			final URLConnection conn = aURL.openConnection();
			conn.addRequestProperty("Cookie", cookie);
			conn.connect();
			final BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), defaultBufferSize);
			final Bitmap bm = BitmapFactory.decodeStream(bis);
			bis.close();
			return bm;
		} catch (OutOfMemoryError error) {
			Logger.error("Out of Memory Error for url " + url, error);
		} catch (IOException e) {
			Logger.error("Problems loading Bitmap " + e.getMessage());
		}
		return null;
	}

	public static Bitmap loadBitmap(InputStream input) {
		try {
			return BitmapFactory.decodeStream(input, null, sBitmapOptions);
		} catch (OutOfMemoryError error) {
			Logger.error("Out of Memory Error", error);
		}
		return null;
	}

	public static void setBitmapCache(String path, Bitmap bitmap) {
		bitmapCache.put(path, bitmap);
	}

	public static Bitmap getBitmapCache(String path) {
		return bitmapCache.get(path);
	}

	public static boolean hasBitmapCacheEntry(String path) {
		return bitmapCache.containsKey(path);
	}

	public static void clearBitmapCache() {
		//		for (Bitmap bitmap : bitmapCache.values()) {
		//			if (bitmap != null) {
		//				bitmap.recycle();
		//			}
		//		}
		bitmapCache.clear();
	}

	public static Drawable ImageOperations(String url) {
		try {
			if (cache.get(url) != null) {
				return cache.get(url);
			}
			InputStream is = (InputStream) fetch(url);
			Drawable d = Drawable.createFromStream(is, "src");
			cache.put(url, d);
			return d;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void clearCache() {
		cache.clear();
	}

	private static Object fetch(String address) throws MalformedURLException, IOException {
		URL url = new URL(address);
		Object content = url.getContent();
		return content;
	}

	public static void setBitmapCache(String cache, String path, Bitmap bitmap) {
		HashMap<String, Bitmap> map = caches.get(cache);
		if (map == null) {
			map = new HashMap<String, Bitmap>();
			caches.put(cache, map);
		}
		map.put(path, bitmap);
	}

	public static Bitmap getBitmapCache(String cache, String path) {
		HashMap<String, Bitmap> map = caches.get(cache);
		if (map == null) {
			return null;
		}
		return map.get(path);
	}

	public static boolean hasBitmapCacheEntry(String cache, String path) {
		HashMap<String, Bitmap> map = caches.get(cache);
		if (map == null) {
			return false;
		}
		return map.containsKey(path);
	}

	public static void clearBitmapCache(String cache) {
		HashMap<String, Bitmap> map = caches.get(cache);
		if (map == null) {
			return;
		}
		//		for (Bitmap bitmap : map.values()) {
		//			if (bitmap != null) {
		//				bitmap.recycle();
		//			}
		//		}
		map.clear();
		caches.remove(cache);
	}

	/**
	 * Save a bitmap to a file
	 */
	protected void saveBitmapToFile(final File dest, final Bitmap bitmapToSave) {
		if (bitmapToSave == null || dest == null) {
			Logger.error(this, "saveBitmapToFile one of these is missing: bitmap " + bitmapToSave + " File " + dest);
			return;
		}
		try {
			FileOutputStream out = new FileOutputStream(dest);
			bitmapToSave.compress(Bitmap.CompressFormat.JPEG, JPG_QUALITY, out);
			out.flush();
			out.close();
		} catch (OutOfMemoryError e) {
			Logger.error("saveBitmapToFile:Out of memory error", e);
			//			((ViaMeApplication) getApplicationContext()).handleOutOfMemory(e);
		} catch (Exception e) {
			Logger.error("Error saving bitmap to file " + e.getMessage(), e);
		}
	}

	/**
	 * Create a cached File name given an optional prefix and/or suffix
	 *
	 * @return filename
	 */
	public static String getCachedFileName(Context context, String prefix, String suffix) throws IOException {
		File mCacheDir = new File(context.getCacheDir(), CACHE_DIR);
		if (!mCacheDir.exists()) {
			if (!mCacheDir.mkdirs()) {
				throw new IOException("Cannot create cache directory: " + mCacheDir.getAbsolutePath());
			}
		}
		StringBuilder fileName = new StringBuilder();
		if (prefix != null) {
			fileName.append(prefix);
		}
		fileName.append(sDateFormat.format(new Date()));
		if (suffix != null) {
			fileName.append(suffix);
		}
		File tempFile = new File(mCacheDir, fileName.toString());
		return tempFile.getAbsolutePath();
	}

	/**
	 * Delete all cache files
	 */
	public static void deleteCachedFiles(Context context) {
		File mCacheDir = new File(context.getCacheDir(), CACHE_DIR);
		if (mCacheDir.exists()) {
			deleteFiles(mCacheDir);
		}

	}

	/**
	 * Recursively Delete a directory.
	 */
	public static void deleteFiles(File dir) {
		if (dir != null) {
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isFile()) {
					if (!file.delete()) {
						Logger.error("Problems deleting cache file " + file.getAbsolutePath());
					}
				} else {
					deleteFiles(file);
				}
			}
		}
	}

	public static void saveBitmap(String filename, Bitmap bitmap) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filename);
			bitmap.compress(Bitmap.CompressFormat.JPEG, JPG_QUALITY, fos);
		} catch (FileNotFoundException e) {
			Logger.error("File not found for " + filename, e);
		}
	}

	/**
	 * Gets the width and height for a bitmap for a uri
	 *
	 * @param context
	 * @param uri
	 *
	 * @return Rect
	 */
	public static Rect getBitmapRect(Context context, Uri uri)
	{
		// Decode the image
		try
		{
			// Prepare the decode options to just load the bounds
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;

			// Load the information
			InputStream is = context.getContentResolver().openInputStream(uri);
			BitmapFactory.decodeStream(is, null, options);

			// Invert the sizes if 90 or 270 degrees
			int orientation = getImageOrientation(context, uri);
			orientation %= 360;
			orientation /= 90;
			if (orientation % 2 != 0)
			{
				// Rotated by 90 or 270 degrees, invert width and height
				return new Rect(0, 0, options.outHeight, options.outWidth);
			}
			else
			{
				// Rotated by 0 or 180, do not invert
				return new Rect(0, 0, options.outWidth, options.outHeight);
			}
		}
		catch(OutOfMemoryError ex)
		{
			Logger.error("Out of memory while loading bitmap of " + uri.toString(), ex);
			return null;
		}
		catch (FileNotFoundException ex)
		{
			Logger.error("Could not get bounds of bitmap " + uri.toString(), ex);
			return null;
		}
	}

	/**
	 * Gets the width and height for a bitmap on the file system
	 *
	 * @param data
	 *
	 * @return Rect
	 */
	public static Rect getBitmapRect(byte[] data)
	{
		// Decode the image
		try
		{
			// Prepare the decode options to just load the bounds
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;

			// Load the information
			BitmapFactory.decodeByteArray(data, 0, data.length, options);

			return new Rect(0, 0, options.outWidth, options.outHeight);
		}
		catch(OutOfMemoryError ex)
		{
			Logger.error("Out of memory while loading bitmap", ex);
			return null;
		}
	}

	/**
	 * Gets the image rotation from the MediaStore data base
	 *
	 * @param context
	 * @param uri
	 *
	 * @return int
	 */
	static public int getImageOrientation(Context context, Uri uri)
	{
		// Not need to log, getMediaStoreValue will have done it for us
		String orientation = getMediaStoreValue(context, uri, MediaStore.Images.ImageColumns.ORIENTATION);

		// Generated thumbnails or downloaded files will not appear in the media store.
		// So the call above will return null, which is OK.
		if (orientation == null)
		{
			return 0;
		}

		// Parse the value to an int
		try
		{
			return Integer.parseInt(orientation);
		}
		catch(NumberFormatException ex)
		{
			Logger.warn("Obtained a non-integer orientation from the MediaStore: " + orientation);
			return 0;
		}
	}

	/**
	 * General utility to get a value from the MediaStore database
	 *
	 * @param context
	 * @param uri
	 * @param column
	 *
	 * @return String The queried value, or null if not found
	 */
	public static String getMediaStoreValue(Context context,
											Uri uri,
											String column)
	{
		String result = null;

		// We will log errors only if the uri starts with "content".  If it starts with file,
		// it is possible that we are querying for a generated thumbnail or downloaded file,
		// which are NOT part of the media store.
		boolean logError = uri.getScheme().equalsIgnoreCase(ContentResolver.SCHEME_CONTENT);

		// Prepare and run the query
		String[] columns = new String[]
			{
				column,
			};
		Cursor c = context.getContentResolver().query(uri, columns, null, null, null);

		// Check if we caught something
		if (c != null)
		{
			if (c.moveToFirst())
			{
				result = c.getString(0);
			}
			else if (logError)
			{
				Logger.error("Could not get \"" + column + "\" column from MediaStore for " + uri);
			}
			c.close();
		}
		else if (logError)
		{
			Logger.error("Could not get \"" + column + "\" column from MediaStore for " + uri);
		}

		return result;
	}


}

package com.mastertechsoftware.media;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.mastertechsoftware.util.log.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageUtils {
	public static long kilobytes = 1000; // 1,000 bytes
	public static long megabytes = kilobytes * 1000;   // 1,000,000
	public static long gigabytes = megabytes * 1000;   // 1,000,000,000

	/**
	 * Create a scaled bitmap. Note this method does not recycle the src bitmap
	 * @param src
	 * @param dstWidth
	 * @param dstHeight
	 * @param filter
	 * @return new Bitmap
	 */
	public static Bitmap CreateScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter)
	{
	    Matrix m = new Matrix();
	    
	    m.setScale(dstWidth  / (float)src.getWidth(), dstHeight / (float)src.getHeight());
		try {
			Bitmap result = Bitmap.createBitmap(dstWidth, dstHeight, src.getConfig());
			Canvas canvas = new Canvas(result);

				Paint paint = new Paint();
				paint.setFilterBitmap(filter);
				canvas.drawBitmap(src, m, paint);

			return result;
		} catch (OutOfMemoryError e) {
			Logger.error("CreateScaledBitmap: OOM", e);
		}
		return null;
	}

	/**
	 * Get a file in the picture directory with the given name
	 * @param albumName
	 * @return album file
	 */
	public static File getStorageDir(String albumName) {

		if (android.os.Build.VERSION.SDK_INT < 8) {
			return new File(Environment.getExternalStorageDirectory() + "/dcim/" + albumName);
		}

		return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
	}

	/**
	 * Create a new File object pointing to a ViaMe directory inside of the picture folder
	 * @return
	 */
	public static File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

			storageDir = getStorageDir("ViaMe");

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Logger.debug("failed to create directory");
						return null;
					}
				}
			}

		} else {
			Logger.error("External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	/**
	 * Create a temp file inside of the ViaMe picture folder
	 * @return
	 * @throws IOException
	 */
	public static File createTempImageFile(Context context) throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "ViaMe_" + timeStamp + "_";
		File albumDir = getAlbumDir();
		if (albumDir != null) {
			return File.createTempFile(imageFileName, ".jpg", albumDir);
		}
		File cacheDir = context.getCacheDir();
		if (cacheDir != null) {
			return File.createTempFile(imageFileName, ".jpg", cacheDir);
		}
		return null;
	}

	/**
	 * Add a picture file to the camera's gallery
	 * @param context
	 * @param pictureFile
	 */
	public static void addFileToPictures(Context context, File pictureFile) {
		if (pictureFile == null) {
			Logger.error("addFileToPictures: Picture file is null");
			return;
		}
		// Tell the media scanner about the new file so that it is
		// immediately available to the user.
		MediaScannerConnection.scanFile(context, new String[]{pictureFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
			public void onScanCompleted(String path, Uri uri) {
				Logger.debug("ExternalStorage", "Scanned " + path + ":");
				Logger.debug("ExternalStorage", "-> uri=" + uri);
			}

		});
	}

	/**
	 * Create a Video Image File
	 * @param context
	 * @param videoFilePath
	 * @return path to temp file
	 */
	public static String createVideoImageFile(Context context, String videoFilePath) {
		Bitmap bm = null;

		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inSampleSize = 1;
		bm=android.media.ThumbnailUtils.createVideoThumbnail(videoFilePath, MediaStore.Video.Thumbnails.MINI_KIND);
		if (bm != null) {
			//						Logger.debugNow("Created Thumbnail");
			OutputStream os = null;
			try {
				File file = ImageUtils.createTempImageFile(context);
				os = new FileOutputStream(file);
				os = new BufferedOutputStream(os, 8192);
				bm.compress(Bitmap.CompressFormat.PNG, 90, os);
				os.flush();
				os.close();
				return file.getAbsolutePath();
			} catch (FileNotFoundException e) {
				Logger.error("Problems creating video thumbnail", e);
			} catch (IOException e) {
				Logger.error("Problems creating video thumbnail", e);
			} finally {
				// Force release the bitmap
				bm.recycle();
			}
		}
		return null;
	}

	/**
	 * Load in an bitmap from the given path (can be http or file based).
	 * @param path
	 * @param width
	 * @param height
	 * @return Bitmap
	 */
	public static Bitmap loadBitmapImage(String path, int width, int height) {
		InputStream inputStream = null;
		// decodeStream closes inputstream
		try {
			//Decode image size
			if (path.startsWith("http")) {
				URL url = new URL(path);
				URLConnection urlConnection = url.openConnection();
				inputStream = new BufferedInputStream(urlConnection.getInputStream());
				BitmapFactory.Options options = readOptions(inputStream);
				inputStream.close();
				urlConnection = url.openConnection();
				inputStream = new BufferedInputStream(urlConnection.getInputStream());
				return decodeStream(inputStream, options, width, height);
			} else {
				File file = new File(path);
				if (!file.exists()) {
					return null;
				}
				BitmapFactory.Options options = readOptions(path);

				inputStream = new BufferedInputStream(new FileInputStream(file));
				return decodeStream(inputStream, options, width, height);
			}
		} catch (OutOfMemoryError e) {
			Logger.error("Problems loading " + path, e);
		} catch (IOException e) {
			Logger.error("Problems loading " + path, e);
		}
		return null;
	}

	/**
	 * Load in an bitmap from the given path (can be http or file based).
	 * @param path
	 * @param scale
	 * @return Bitmap
	 */
	public static Bitmap loadBitmapImage(String path, int scale) {
		InputStream inputStream = null;
		// decodeStream closes inputstream
		try {
			//Decode image size
			if (path.startsWith("http")) {
				URL url = new URL(path);
				URLConnection urlConnection = url.openConnection();
				inputStream = new BufferedInputStream(urlConnection.getInputStream());
				inputStream.close();
				urlConnection = url.openConnection();
				inputStream = new BufferedInputStream(urlConnection.getInputStream());
				return decodeStream(inputStream, scale);
			} else {
				File file = new File(path);
				if (!file.exists()) {
					return null;
				}
				inputStream = new BufferedInputStream(new FileInputStream(file));
				return decodeStream(inputStream, scale);
			}
		} catch (OutOfMemoryError e) {
			Logger.error("Problems loading " + path, e);
		} catch (IOException e) {
			Logger.error("Problems loading " + path, e);
		}
		return null;
	}

	/**
	 * Get the orientation of a photo
	 * @param path
	 * @return 0, 90, 180, or 270
	 */
	public static int getOrientation(String path) {
		ExifInterface exif;
		int angle = 0;

		try {
			exif = new ExifInterface(path);

			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			//            Logger.debug(this, "Orientation in crop image: " + orientation);
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					angle = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					angle = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					angle = 270;
					break;
				case ExifInterface.ORIENTATION_NORMAL:
					break;

				// The user of this method can check the width/height to determine if they want to do something different
				case 0:

//					int width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
//					int height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);
//
//					if (width > height) {
//						angle = 90;
//					}
					break;
				default:
					angle = 0;
					break;
			}
		} catch (IOException e1) {
			Logger.error("Problems loading " + path, e1);
		}
		return angle;
	}

	/**
	 * Get the scaling factor for a bitmap
	 *
	 * @return scale number used in inSampleSize
	 */
	public static int getScale(int originalWidth, int originalHeight, int width, int height) {
		int scale = 1;
		int width_tmp = originalWidth, height_tmp = originalHeight;
		if (height_tmp > height || width_tmp > width) {
			int widthScale = 1;
			while ((width_tmp) > width) {
				width_tmp /= 2;
				widthScale *= 2;
			}
			int heightScale = 1;
			while ((height_tmp) > height) {
				height_tmp /= 2;
				heightScale *= 2;
			}
			scale = Math.min(widthScale, heightScale);
		} else {
			scale = 2; // Factor of 2 smaller
		}
		return scale;
	}

	/**
	 * Utility function for creating a scaled version of an existing bitmap
	 *
	 * @param unscaledBitmap Bitmap to scale
	 * @param dstWidth Wanted width of destination bitmap
	 * @param dstHeight Wanted height of destination bitmap
	 * @return New scaled bitmap object
	 */
	public static Bitmap createScaledBitmap(Bitmap unscaledBitmap, int dstWidth, int dstHeight) {
		Rect srcRect = new Rect(0, 0, unscaledBitmap.getWidth(), unscaledBitmap.getHeight());
		Rect dstRect = calculateDstRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(),
			dstWidth, dstHeight);
//		Logger.debugNow("DstRect " + dstRect);
		Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(),
			Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(scaledBitmap);
		canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, new Paint(Paint.FILTER_BITMAP_FLAG));

		return scaledBitmap;
	}

	/**
	 * Calculate optimal down-sampling factor given the dimensions of a source
	 * image, the dimensions of a destination area and a scaling logic.
	 *
	 * @param srcWidth Width of source image
	 * @param srcHeight Height of source image
	 * @param dstWidth Width of destination area
	 * @param dstHeight Height of destination area
	 * @return Optimal down scaling sample size for decoding
	 */
	public static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
		final float srcAspect = (float)srcWidth / (float)srcHeight;
		final float dstAspect = (float)dstWidth / (float)dstHeight;

		if (srcAspect > dstAspect) {
			return Math.round(srcWidth / dstWidth);
		} else {
			return Math.round(srcHeight / dstHeight);
		}
	}

	/**
	 * Calculates destination rectangle for scaling bitmap
	 *
	 * @param srcWidth Width of source image
	 * @param srcHeight Height of source image
	 * @param dstWidth Width of destination area
	 * @param dstHeight Height of destination area
	 * @return Optimal destination rectangle
	 */
	public static Rect calculateDstRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
		final float srcAspect = (float)srcWidth / (float)srcHeight;
		final float dstAspect = (float)dstWidth / (float)dstHeight;
//		Logger.debugNow("srcAspect " + srcAspect + " dstAspect " + dstAspect);

		if (srcAspect > dstAspect) {
			return new Rect(0, 0, dstWidth, (int)(dstWidth / srcAspect));
		} else {
			return new Rect(0, 0, (int)(dstHeight * srcAspect), dstHeight);
		}
	}

	/**
	 * Read in the options for a bitmap
	 * @param path
	 * @return BitmapFactory.Options
	 * @throws OutOfMemoryError
	 * @throws IOException
	 */
	public static BitmapFactory.Options readOptions(String path)  throws OutOfMemoryError, IOException {
		BitmapFactory.Options options = new BitmapFactory.Options();
		try {
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, options);
			return options;
		} catch (OutOfMemoryError e) {
			Logger.error("readOptions OutOfMemoryError for path " + path);
			throw e;
		}
	}

	/**
	 * Read in the options for a bitmap
	 * @param inputStream
	 * @return BitmapFactory.Options
	 * @throws OutOfMemoryError
	 * @throws IOException
	 */
	public static BitmapFactory.Options readOptions(InputStream inputStream)  throws OutOfMemoryError, IOException {
		BitmapFactory.Options options = new BitmapFactory.Options();
		try {
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(inputStream, null, options);
			return options;
		} catch (OutOfMemoryError e) {
			Logger.error("readOptions OutOfMemoryError");
			throw e;
		}
	}

	/**
	 * Read in the options for the given URL
	 * @param url
	 * @return BitmapFactory.Options
	 * @throws OutOfMemoryError
	 * @throws IOException
	 */
	public static BitmapFactory.Options readOptions(URL url)  throws OutOfMemoryError, IOException {
		BitmapFactory.Options options = new BitmapFactory.Options();
		InputStream inputStream = null;
		try {
			URLConnection urlConnection = url.openConnection();
			inputStream = new BufferedInputStream(urlConnection.getInputStream());
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(inputStream, null, options);
			return options;
		} catch (OutOfMemoryError e) {
			Logger.error("readOptions OutOfMemoryError for url " + url);
			throw e;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					Logger.error("readOptions IOException for url " + url);
					throw e;
				}
			}
		}
	}

	/**
	 * Read Options using the asset Manager
	 * @param assetManager
	 * @param path
	 * @return BitmapFactory.Options
	 * @throws OutOfMemoryError
	 * @throws IOException
	 */
	public static BitmapFactory.Options readOptions(AssetManager assetManager, String path)  throws OutOfMemoryError, IOException {
		BitmapFactory.Options options = new BitmapFactory.Options();
		InputStream inputStream = null;
		try {
			inputStream = new BufferedInputStream(assetManager.open(path));
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(inputStream, null, options);
			return options;
		} catch (OutOfMemoryError e) {
			Logger.error("readOptions OutOfMemoryError for path " + path);
			throw e;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					Logger.error("readOptions IOException for path " + path);
					throw e;
				}
			}
		}
	}

	/**
	 * Intelligently read in a bitmap by scaling if necessary
	 * @param inputStream
	 * @param options
	 * @param width
	 * @param height
	 * @return Bitmap
	 * @throws OutOfMemoryError
	 * @throws IOException
	 */
	public static Bitmap decodeStream(InputStream inputStream, BitmapFactory.Options options, int width, int height) throws OutOfMemoryError, IOException {
		int scale = 1;
		try {
			if (options.outHeight > height || options.outWidth > width) {
				scale = getScale(options.outWidth, options.outHeight, width, height);
			}
			//			Logger.debug(this, "getBitmap: scale: " + scale);
			//			Logger.debug(this, "getBitmap: width: " + options.outWidth + " height: " + options.outHeight);

			options.inJustDecodeBounds = false;
			options.inSampleSize = scale;
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
			//			Logger.debugLocal(ApplyFilterActivity.class.getSimpleName(),
			//				"Creating Bitmap of size " + ImageUtils.getPrettyAmount(bitmap.getWidth() * bitmap.getHeight() * 4));

			return bitmap;
		} catch (OutOfMemoryError e) {
			Logger.error("decodeStream OutOfMemoryError with scale " + scale + " and width " + width + " height " + height);
			throw e;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					Logger.error("decodeStream IOException");
					throw e;
				}
			}
		}
	}

	/**
	 * Read in a bitmap with the given scale
	 * @param inputStream
	 * @param scale
	 * @return Bitmap
	 * @throws OutOfMemoryError
	 * @throws IOException
	 */
	public static Bitmap decodeStream(InputStream inputStream, int scale) throws OutOfMemoryError, IOException {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
			options.inSampleSize = scale;
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
			//			Logger.debugLocal(ApplyFilterActivity.class.getSimpleName(),
			//				"Creating Bitmap of size " + ImageUtils.getPrettyAmount(bitmap.getWidth() * bitmap.getHeight() * 4));

			return bitmap;
		} catch (OutOfMemoryError e) {
			Logger.error("decodeStream OutOfMemoryError with scale " + scale);
			throw e;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					Logger.error("decodeStream IOException");
					throw e;
				}
			}
		}
	}
	/**
     * This method converts dp unit to equivalent device specific value in pixels.
     *
     * @param dp A value in dp(Device independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent Pixels equivalent to dp according to device
     */
    public static float convertDpToPixel(float dp,Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
		return dp * (metrics.density) + 0.5f;
    }

    /**
     * This method converts device specific pixels to device independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent db equivalent to px value
     */
    public static float convertPixelsToDp(float px,Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;

    }

	public static boolean checkHeapSize(long requestedSize, long maxSize) {
		long allocNativeHeap = Debug.getNativeHeapAllocatedSize();
		long totalMemory = Runtime.getRuntime().totalMemory();
		long freeMemory = Runtime.getRuntime().freeMemory();
		long currentAllocatedHeapSize = 0;
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			currentAllocatedHeapSize = totalMemory - freeMemory;
		} else {
			currentAllocatedHeapSize = allocNativeHeap;
		}
		Logger.debug(
			"Native Heap Size: " + getPrettyAmount(allocNativeHeap) + " Requested: " + getPrettyAmount(allocNativeHeap + requestedSize));
		if ((currentAllocatedHeapSize + requestedSize) < maxSize) {
			return true;
		}
		return false;
	}

	public static long getAllocatedHeapSize() {
		long allocNativeHeap = Debug.getNativeHeapAllocatedSize();
		long totalMemory = Runtime.getRuntime().totalMemory();
		long freeMemory = Runtime.getRuntime().freeMemory();
		long currentAllocatedHeapSize = 0;
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			currentAllocatedHeapSize = totalMemory - freeMemory;
		} else {
			currentAllocatedHeapSize = allocNativeHeap;
		}
		return currentAllocatedHeapSize;
	}

	public static long getAvailHeapSize() {
		long currentAllocatedHeapSize = getAllocatedHeapSize();
		long systemMaxMemory = Runtime.getRuntime().maxMemory();
		return (systemMaxMemory - currentAllocatedHeapSize);
	}

	public static  void printMemory() {
		Logger.debugNow("Native Heap Size: " + getPrettyAmount(Debug.getNativeHeapSize()));
		Logger.debugNow("Native Allocated Heap Size: " + getPrettyAmount(Debug.getNativeHeapAllocatedSize()));
		Logger.debugNow("Native Free Heap Size: " + getPrettyAmount(Debug.getNativeHeapFreeSize()));
		long freeMemory = Runtime.getRuntime().freeMemory();
		Logger.debugNow("Runtime freeMemory: " + getPrettyAmount(freeMemory));
		long totalMemory = Runtime.getRuntime().totalMemory();
		long maxMemory = Runtime.getRuntime().maxMemory();
		Logger.debugNow("Runtime totalMemory: " + getPrettyAmount(totalMemory));
		Logger.debugNow("Runtime maxMemory: " + getPrettyAmount(maxMemory));
	}

	public static  void printLargeMemory() {
		Logger.debugNow("Native Heap Size: " + getLargeSizeString(Debug.getNativeHeapSize()));
		Logger.debugNow("Native Allocated Heap Size: " + getLargeSizeString(Debug.getNativeHeapAllocatedSize()));
		Logger.debugNow("Native Free Heap Size: " + getLargeSizeString(Debug.getNativeHeapFreeSize()));
	}

	public static String getPrettyAmount(long amount) {
		StringBuilder builder = new StringBuilder();
		builder.append(amount).append(" Total Bytes, ");
		if (amount > (gigabytes)) {
			long gigs = amount / gigabytes;
			builder.append(gigs).append(" GB, ");
			amount -= (gigs * gigabytes);
		}
		if (amount > (megabytes)) {
			long megs = amount / megabytes;
			builder.append(megs).append(" MB, ");
			amount -= (megs * megabytes);
		}
		if (amount > (kilobytes)) {
			long kilos = amount / kilobytes;
			builder.append(kilos).append(" KB, ");
			amount -= (kilos * kilobytes);
		}
		if (amount > 0) {
			builder.append((amount)).append(" B ");
		}
		return builder.toString();
	}

	public static String getLargeSizeString(long amount) {
		StringBuilder builder = new StringBuilder();
		boolean printed = false;
		if (amount > (gigabytes)) {
			long gigs = amount / gigabytes;
			builder.append(gigs).append(" GB, ");
			amount -= (gigs * gigabytes);
			printed = true;
		}
		if (amount > (megabytes)) {
			long megs = amount / megabytes;
			builder.append(megs).append(" MB ");
			amount -= (megs * megabytes);
			printed = true;
		}
		if (!printed) {
			if (amount > (kilobytes)) {
				long kilos = amount / kilobytes;
				builder.append(kilos).append(" KB ");
				amount -= (kilos * kilobytes);
			}
		}
		return builder.toString();
	}

	public static void printLayoutDimensions(ViewGroup rootView) {
		Rect rect = new Rect();
		rootView.getHitRect(rect);
		Logger.debug("View " + rootView.getClass().getSimpleName() + " has size: " + rect.toShortString());
		int childCount = rootView.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View childAt = rootView.getChildAt(i);
			childAt.getHitRect(rect);
			Logger.debug("View " + childAt.getClass().getSimpleName() + " has size: " + rect.toShortString());
			if (childAt instanceof ViewGroup) {
				printLayoutDimensions((ViewGroup) childAt);
			}
		}
	}
}

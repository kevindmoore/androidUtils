package com.mastertechsoftware.util.image;

import com.mastertechsoftware.util.log.Logger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

public class ImageUtil {
	protected static int defaultBufferSize = 8192;
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
         } catch (IOException e) {
              Log.d("DEBUGTAG", "Problems loading Bitmap " + e.getMessage());
         }
         return null;
    }

    public static Bitmap loadBitmap(InputStream input) {
		try {
        	return BitmapFactory.decodeStream(input, null, sBitmapOptions);
		} catch (OutOfMemoryError error) {
			Logger.error("ImageUtil", "loadBitmap Failed", error);
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
	
	private static Object fetch(String address) throws MalformedURLException,IOException {
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


}

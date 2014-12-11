package com.mastertechsoftware.util.image;

import android.graphics.Bitmap;

import com.mastertechsoftware.util.log.Logger;

import java.util.TreeMap;

/**
 * @author Kevin Moore
 *         Copyright (c) 1996-2010 by Cisco Systems, Inc.
 *         All rights reserved. Cisco confidential
 */
public class SmartMediaCache {
	private static final int KILO_BYTE = 1024;
	private static final int MEGA_BYTE = KILO_BYTE * KILO_BYTE;
	private static final int MAX_CACHE_MEMORY = 13 * MEGA_BYTE;  // I'm told 16MB is the max on most devices
	private static final int MIN_CACHED_ITEMS = 30;
	private static final int DESIRED_CACHED_ITEMS = 60;
	private static final int DESIRED_CACHED_ITEMS_LOMEM = 50;
	private static int maxCachedItems;
	protected static TreeMap<String,CacheItem> bitmapCache = new TreeMap<String,CacheItem>();
	protected static TreeMap<Long,String> bitmapKeyCache = new TreeMap<Long,String>();
	protected static TreeMap<String,CacheItem> nonRemoveablebitmapCache = new TreeMap<String,CacheItem>();
	protected static SmartMediaCache instance;

//	protected int bitmapSizeCount = 0;

	// TODO - Remove these when we're finished testing
//	private static final SimpleDateFormat durationFormatter = new SimpleDateFormat("hh:mm:ss:SS");
//	protected Calendar printDate = Calendar.getInstance();

	/**
	 * Singleton retriever
	 * @return SmartMediaCache
	 */
	public static SmartMediaCache getInstance() {
		if (instance == null) {
			instance = new SmartMediaCache();
		}
		return instance;
	}

	/**
	 * Private constructor so no one else can create another instance of this class
	 */
	private SmartMediaCache() {
        maxCachedItems = DESIRED_CACHED_ITEMS;
	}

	/**
	 * Set the max cached items. Pass getRowBytes() * getHeight() for the bitmap size you want
	 * @param bitmapSize
	 */
	public static void setMaxCachedItems(int bitmapSize) {
		maxCachedItems = Math.max(MAX_CACHE_MEMORY/bitmapSize, MIN_CACHED_ITEMS);

	}

	/**
	 * If we're out of memory, reconfigure out our max cache items
	 */
	public void handleOutOfMemoryError() {
		Logger.error("SmartMediCache:handleOutOfMemoryError");
//		double freeHeap = (double)Debug.getNativeHeapSize();
//		int bitmapSize = 4 * MediaImageLoader.THUMBNAIL_WIDTH * MediaImageLoader.THUMBNAIL_HEIGHT;
//		Logger.debug("freeHeap Bitmaps: " + freeHeap/bitmapSize);
//		MiscUtils.logHeap(this.getClass());

		// For now, just decrements by 5
		maxCachedItems = Math.max((maxCachedItems-5), MIN_CACHED_ITEMS);
//		maxCachedItems = (int)freeHeap/bitmapSize;
//		cleanupImages();
	}

	private void cleanupImages() {
		Logger.debug("cleanupImages");
		synchronized (this) {
			for (String id : bitmapCache.keySet()) {
				CacheItem cacheItem = bitmapCache.get(id);
				Bitmap bitmap = cacheItem.getBitmap();
				if (bitmap != null && bitmap.isRecycled()) {
					Logger.debug("cleanupImages: Found recycled bitmap for " + id);
					removeCachedKey(cacheItem.getLastAccessed());
					bitmapCache.remove(id);
					cacheItem.sendRemovedEvent();
				}
			}
		}
	}


	/**
	 * Get the cached bitmap for the given id or null if not cached
	 * @param id
	 * @return Bitmap
	 */
	public Bitmap getCachedBitmap(String id) {
		synchronized (this) {
			CacheItem cacheItem = bitmapCache.get(id);
			if (cacheItem != null) {
				long lastAccessed = cacheItem.getLastAccessed();
				cacheItem.updateLastAccessed();
				updateCacheKey(lastAccessed, cacheItem.getLastAccessed(), id);
				Bitmap bitmap = cacheItem.getBitmap();
				if (bitmap != null) {
					return bitmap;
				} else {
					Logger.debug("getCachedBitmap: Found cache item but bitmap was null for " + id);
				}
			}
		}
		return null;
	}

	/**
	 * Get the non-removeable cached bitmap for the given id or null if not cached
	 * @param id
	 * @return Bitmap
	 */
	public Bitmap getNonRemoveableCachedBitmap(String id) {
		synchronized (this) {
			CacheItem cacheItem = nonRemoveablebitmapCache.get(id);
			if (cacheItem != null) {
				return cacheItem.getBitmap();
			}
		}
		return null;
	}

	/**
	 * Add a new bitmap to our cache
	 * @param id
	 * @param bitmap
	 * @param listener
	 */
	public void addCachedBitmap(String id, Bitmap bitmap, BitmapRemovedListener listener) {
//		if (maxCachedItems == MIN_CACHED_ITEMS) {
//			setMaxCacheMemory();
//		}
		CacheItem cacheItem = null;
		synchronized (this) {
			cacheItem = bitmapCache.get(id);
			if (cacheItem != null) {
				long lastAccessed = 0;
				cacheItem.setBitmap(bitmap);
				lastAccessed = cacheItem.getLastAccessed();
				cacheItem.updateLastAccessed();
//					printDate.setTimeInMillis(cacheItem.getLastAccessed());
//					String format = durationFormatter.format(printDate.getTime());
//					Logger.debug("addCachedBitmap:Updating bitmap with time stamp of: " + format + " and id of " + id);
				if (cacheItem.getListener() != listener) {
					cacheItem.sendRemovedEvent();
				}
				cacheItem.setListener(listener); // This could change
				updateCacheKey(lastAccessed, cacheItem.getLastAccessed(), id);
			}
		}
		if (cacheItem == null) {
			if ((bitmapCache.size() + 1) > maxCachedItems) {
				String lastAccessedId = null;
				Long lastAccessed = null;
				CacheItem cachedItemTobeRemoved = null;
				synchronized (this) {
					lastAccessed = bitmapKeyCache.firstKey();
					lastAccessedId = bitmapKeyCache.get(lastAccessed);
					cachedItemTobeRemoved = bitmapCache.get(lastAccessedId);
					if (lastAccessedId != null && cachedItemTobeRemoved != null) {
//							printDate.setTimeInMillis(lastAccessed);
//							String format = durationFormatter.format(printDate.getTime());
//							Logger.debug("Removing bitmap with time stamp of: " + format + " and id of " + lastAccessedId);
//						Bitmap cachedItemTobeRemovedBitmap = cachedItemTobeRemoved.getBitmap();
//						if (cachedItemTobeRemovedBitmap != null) {
//							int bitmapSize = 4 * cachedItemTobeRemovedBitmap.getWidth() * cachedItemTobeRemovedBitmap.getHeight();
//							bitmapSizeCount -= bitmapSize;
//						}
						removeCachedBitmap(lastAccessed, lastAccessedId);
					} else {
						if (cachedItemTobeRemoved == null) {
							Logger.error("cachedItemTobeRemoved is null of lastAccessedId= " + lastAccessedId);
						}
						Logger.error("Could not remove bitmap id of " + lastAccessedId);
						removeCachedKey(lastAccessed);
					}
				}
			} // if bitmapCache.size() + 1)
			synchronized (this) {
				cacheItem = new CacheItem(id, bitmap, listener);
				bitmapCache.put(id, cacheItem);
				addKeyCache(cacheItem.getLastAccessed(), id);
//				int bitmapSize = 4 * bitmap.getWidth() * bitmap.getHeight();
//				long freeMemory = Runtime.getRuntime().freeMemory();
//				long totalMemory = Runtime.getRuntime().totalMemory();
//				Logger.debug("freeMemory: " + freeMemory);
//				bitmapSizeCount += bitmapSize;
//				Logger.debug(bitmapCache.size() + " Bitmaps with size: " + bitmapSizeCount);
//				Logger.debug("Total Memory " + (freeMemory + bitmapSizeCount));
//				Logger.debug("Total Free Memory " + (totalMemory - bitmapSizeCount));
//				printDate.setTimeInMillis(cacheItem.getLastAccessed());
//				String format = durationFormatter.format(printDate.getTime());
//				Logger.debug("Adding " + id + " bitmap with time stamp of: " + format);
			}
		}
	}

	/**
	 * Add a new bitmap to our non-removeable cache
	 * @param id
	 * @param bitmap
	 */
	public void addNonRemoveableCachedBitmap(String id, Bitmap bitmap) {
		CacheItem cacheItem = null;
		synchronized (this) {
			cacheItem = nonRemoveablebitmapCache.get(id);
			if (cacheItem != null) {
				cacheItem.setBitmap(bitmap);
			}
			else  {
				cacheItem = new CacheItem(id, bitmap, null);
				nonRemoveablebitmapCache.put(id, cacheItem);
			}
		}
	}

	/**
	 * Add a new last accessed key
	 * @param lastAccessed
	 * @param id
	 */
	private void addKeyCache(Long lastAccessed, String id) {
		synchronized (this) {
			bitmapKeyCache.put(lastAccessed, id);
		}
	}

	/**
	 * Mark a cached item as removeable or not.
	 * @param id
	 * @param removeable
	 */
	public void setCachedBitmapRemovable(String id, boolean removeable) {
		synchronized (this) {
			CacheItem cacheItem = bitmapCache.get(id);
			if (cacheItem != null) {
				cacheItem.setRemovable(removeable);
			} else {
				Logger.error("SmartMediaCache:setCachedBitmapRemovable id " + id + " not found");
			}
		}
	}

	/**
	 * Replace the bitmap with the new bitmap
	 * @param id
	 * @param bitmap
	 */
	public void replaceCachedBitmap(String id, Bitmap bitmap) {
		synchronized (this) {
			CacheItem cacheItem = bitmapCache.get(id);
			if (cacheItem != null) {
				long lastAccessed = 0;
				cacheItem.setBitmap(bitmap);
				lastAccessed = cacheItem.getLastAccessed();
				cacheItem.updateLastAccessed();
				updateCacheKey(lastAccessed, cacheItem.getLastAccessed(), id);
			} else {
				Logger.error("SmartMediaCache:replaceCachedBitmap id " + id + " not found");
			}
		}
	}

	/**
	 * Remove a Cached bitmap
	 * @param lastAccessed
	 * @param id
	 */
	public void removeCachedBitmap(Long lastAccessed, String id) {
//		Logger.debug("SmartMediaCache:removeCachedBitmap id " + id);
		synchronized (this) {
			CacheItem cacheItem = bitmapCache.get(id);
			if (cacheItem != null) {
				removeCachedKey(lastAccessed);
				bitmapCache.remove(id);
				cacheItem.sendRemovedEvent();
				cacheItem.removeBitmap();
			} else {
				Logger.error("SmartMediaCache:removeCachedBitmap id " + id + " not found");
			}
		}
	}

	/**
	 * Remove a Cached bitmap
	 * @param id
	 */
	public void removeCachedBitmap(String id) {
		synchronized (this) {
			CacheItem cacheItem = bitmapCache.get(id);
			if (cacheItem != null) {
				bitmapCache.remove(id);
				removeCachedKey(cacheItem.getLastAccessed());
				cacheItem.sendRemovedEvent();
				cacheItem.removeBitmap();
			} else {
				Logger.error("SmartMediaCache:removeCachedBitmap id " + id + " not found");
			}
		}
	}

	/**
	 * Remove a cached key
	 * @param lastAccessed
	 */
	private void removeCachedKey(Long lastAccessed) {
		synchronized (this) {
			String remove = bitmapKeyCache.remove(lastAccessed);
			if (remove == null) {
				Logger.error("removeCachedKey: could not remove " + lastAccessed + " from bitmapKeyCache");
			}
		}
	}

	/**
	 * Return the current cache size
	 * @return size
	 */
	public int getCurrentCacheSize() {
		return bitmapCache.size();
	}


	/**
	 * What is our maximum cache size
	 * @return max cache size
	 */
	public int getCurrentMaxCacheSize() {
		return maxCachedItems;
	}

	/**
	 * Update the access time for the given id
	 * @param lastAccessed
	 * @param updatedAccess
	 * @param id
	 */
	protected void updateCacheKey(long lastAccessed, long updatedAccess, String id) {
		synchronized (this) {
			removeCachedKey(lastAccessed);
			bitmapKeyCache.put(updatedAccess, id);
		}
	}


	/**
	 * Class for storing cache info
	 */
	class CacheItem implements Comparable<CacheItem> {
		protected String id;
		protected Bitmap bitmap;
		protected long lastAccessed;
		protected boolean removable = true;
		protected BitmapRemovedListener listener;

		CacheItem(String id, Bitmap bitmap, BitmapRemovedListener listener) {
			this.id = id;
			this.bitmap = bitmap;
			this.listener = listener;
			lastAccessed = System.currentTimeMillis();
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Bitmap getBitmap() {
			return bitmap;
		}

		public void setBitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
		}

		public void updateLastAccessed() {
			lastAccessed = System.currentTimeMillis();
		}

		public long getLastAccessed() {
			return lastAccessed;
		}

		public BitmapRemovedListener getListener() {
			return listener;
		}

		public void setListener(BitmapRemovedListener listener) {
			this.listener = listener;
		}

		public void sendRemovedEvent() {
//			if (listener != null && listener.get() != null) {
////				Logger.debug("SmartMediaCache:sendRemovedEvent listener of class " + listener.getClass().toString());
//				listener.get().bitmapRemoved(id, getBitmap());
////			} else {
////				Logger.debug("SmartMediaCache:sendRemovedEvent No Listener" );
//			}
			if (listener != null) {
//				Logger.debug("SmartMediaCache:sendRemovedEvent listener of class " + listener.getClass().toString());
				listener.bitmapRemoved(id, getBitmap());
//			} else {
//				Logger.debug("SmartMediaCache:sendRemovedEvent No Listener" );
			}
		}

		public boolean isRemovable() {
			return removable;
		}

		public void setRemovable(boolean removable) {
			this.removable = removable;
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			return id.equalsIgnoreCase(((CacheItem)obj).id);
		}

		public int compareTo(CacheItem another) {
			return id.compareTo(another.getId());
		}

		public void removeBitmap() {
			Bitmap bitmap = getBitmap();
			if (bitmap != null) {
				bitmap.recycle();
				setBitmap(null);
			}

		}
	}

}

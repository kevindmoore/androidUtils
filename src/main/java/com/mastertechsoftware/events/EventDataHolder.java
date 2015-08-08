package com.mastertechsoftware.events;

import java.util.HashMap;
/**
 * Singleton used to hold data across Activities so the classes don't have to be Parceable.
 */
public class EventDataHolder {
	private static EventDataHolder singleton;
	private HashMap<String, Object> eventData = new HashMap<String, Object>();

	public static EventDataHolder getInstance() {
		if (singleton == null) {
			singleton = new EventDataHolder();
		}
		return singleton;
	}

	private EventDataHolder() {
	}

	/**
	 * Put the given data at the given key
	 * @param key
	 * @param data
	 */
	public void putData(String key, Object data) {
		eventData.put(key, data);
	}

	/**
	 * Return true if it contains the given key
	 * @param key
	 * @return true if it contains the given key
	 */
	public boolean containsKey(String key) {
		return eventData.containsKey(key);
	}

	/**
	 * Get the data stored at the given key and remove it from the list
	 * @param key
	 * @return Object
	 */
	@SuppressWarnings("unchecked")
	public Object getData(String key) {
		return eventData.get(key);
	}

	/**
	 * Return an int value or -1 if not found
	 * @param key
	 * @return
	 */
	public int getInt(String key) {
		Object item = eventData.get(key);
		if (item == null || !(item instanceof Integer)) {
			return -1;
		}
		return (Integer)item;
	}

	/**
	 * Get a string value or null if not found
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		Object item = eventData.get(key);
		if (item == null || !(item instanceof String)) {
			return null;
		}
		return (String)item;
	}

	/**
	 * Get boolean from key
	 * @param key
	 * @return
	 */
	public boolean getBoolean(String key) {
		Object item = eventData.get(key);
		if (item == null || !(item instanceof Boolean)) {
			return false;
		}
		return (Boolean)item;
	}


	/**
	 * Get the data stored at the given key and remove it from the list
	 * @param key
	 * @return Object
	 */
	public Object removeData(String key) {
		return eventData.remove(key);
	}

	/**
	 * Return an int value or -1 if not found
	 * @param key
	 * @return
	 */
	public int removeInt(String key) {
		Object item = eventData.remove(key);
		if (item == null || !(item instanceof Integer)) {
			return -1;
		}
		return (Integer)item;
	}

	/**
	 * Get a string value or null if not found
	 * @param key
	 * @return
	 */
	public String removeString(String key) {
		Object item = eventData.remove(key);
		if (item == null || !(item instanceof String)) {
			return null;
		}
		return (String)item;
	}

	/**
	 * Clear all data. Make sure you are not clearing other activities data.
	 */
	public void clear() {
		eventData.clear();
	}
}

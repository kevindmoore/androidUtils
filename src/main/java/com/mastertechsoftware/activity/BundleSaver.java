package com.mastertechsoftware.activity;

import android.os.Bundle;

import com.google.gson.Gson;
import com.mastertechsoftware.util.log.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
/**
 * Utility class for saving class data to Bundles.
 * Uses reflection to put all fields into bundles via GSON
 */
public class BundleSaver {

	private static final Gson gson = new Gson();

	/**
	 * Save the object to the given key
	 * @param bundle
	 * @param key
	 * @param object
	 */
	public static void saveBundle(Bundle bundle, String key, Object object) {
		if (bundle == null) {
			Logger.error("SaveBundle: bundle is null");
			return;
		}
		bundle.putString(key, parseObject(object));
	}

	/**
	 * Restore a list from the given key & class type
	 * @param bundle
	 * @param key
	 * @param typeClass
	 * @param <T>
	 * @return <T> List<T>
	 */
	public static <T> List<T> toList(Bundle bundle, String key, Class<T> typeClass)
	{
		return gson.fromJson(bundle.getString(key), new ListOfJson<T>(typeClass));
	}

	/**
	 * Parse the given object to Json
	 * @param object
	 * @return
	 */
	public static String parseObject(Object object) {
		return gson.toJson(object);
	}

	/**
	 * Load the object from the given key & type
	 * @param bundle
	 * @param key
	 * @param type
	 * @return Object
	 */
	public static Object loadBundle(Bundle bundle, String key, Class type) {
		if (bundle == null) {
			Logger.error("loadBundle: bundle is null");
			return null;
		}
		return gson.fromJson(bundle.getString(key), type);
	}

	public static class ListOfJson<T> implements ParameterizedType
	{
		private Class<?> wrapped;

		public ListOfJson(Class<T> wrapper)
		{
			this.wrapped = wrapper;
		}

		@Override
		public Type[] getActualTypeArguments()
		{
			return new Type[] { wrapped };
		}

		@Override
		public Type getRawType()
		{
			return List.class;
		}

		@Override
		public Type getOwnerType()
		{
			return null;
		}
	}
}

package com.mastertechsoftware.mvp.model;

import com.mastertechsoftware.util.preferences.Prefs;
/**
 * Concrete class for Saver Interface. Saves to Preferences
 */
public class Saver implements SaverInterface {

	private final Prefs prefs;

	public Saver() {
		// NOTE: Prefs needs to be initialized by Application
		prefs = Prefs.getPrefs();
	}

	@Override
	public void setString(String key, String value) {
		prefs.putString(key, value);
	}

	@Override
	public void setInt(String key, Integer value) {
		prefs.putInt(key, value);

	}

	@Override
	public void setBoolean(String key, Boolean value) {
		prefs.putBoolean(key, value);

	}

	@Override
	public void setDouble(String key, Double value) {
		prefs.putDouble(key, value);

	}

	@Override
	public void setFloat(String key, Float value) {
		prefs.putFloat(key, value);

	}

	@Override
	public void setLong(String key, Long value) {
		prefs.putLong(key, value);

	}

	@Override
	public void setObject(String key, Object value) {
		prefs.putObject(key, value);

	}

	@Override
	public String getString(String key) {
		return prefs.getString(key);
	}

	@Override
	public Integer getInt(String key) {
		return prefs.getInt(key);
	}

	@Override
	public Boolean getBoolean(String key) {
		return prefs.getBoolean(key);
	}

	@Override
	public Double getDouble(String key) {
		return prefs.getDouble(key);
	}

	@Override
	public Float getFloat(String key) {
		return prefs.getFloat(key);
	}

	@Override
	public Long getLong(String key) {
		return prefs.getLong(key);
	}

	@Override
	public Object getObject(String key, Class type) {
		return prefs.getObject(key, type);
	}
}

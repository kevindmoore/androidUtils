package com.mastertechsoftware.mvp.model;

/**
 * Interface for all classes that save model data
 */
public interface SaverInterface {
	void setString(String key, String value);
	void setInt(String key, Integer value);
	void setBoolean(String key, Boolean value);
	void setDouble(String key, Double value);
	void setFloat(String key, Float value);
	void setLong(String key, Long value);
	void setObject(String key, Object value);
	String getString(String key);
	Integer getInt(String key);
	Boolean getBoolean(String key);
	Double getDouble(String key);
	Float getFloat(String key);
	Long getLong(String key);
	Object getObject(String key, Class type);
}

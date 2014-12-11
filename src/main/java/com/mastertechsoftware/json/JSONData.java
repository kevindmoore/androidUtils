package com.mastertechsoftware.json;


import com.mastertechsoftware.util.StackTraceOutput;
import com.mastertechsoftware.util.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
/**
 * Base class for JSON Objects
 */
public class JSONData {

	enum TYPE {
		OBJECT,
		ARRAY,
		VALUE
	}
    public static final String NULL = "null";
	public static final String APPLICATION_ERROR = "Application Error";
	protected String name;
    protected Object value;
    protected TYPE type = TYPE.OBJECT;
    protected List<JSONData> children = new ArrayList<JSONData>();

	/**
	 * Constructors.
	 */
    public JSONData() {
    }

    public JSONData(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public JSONData(String json)  throws JSONDataException {
        parse(json);
    }

    /**
     * Parse a JSONObject recursively.
     * @param json
     */
    public void parse(String json) throws JSONDataException {
        try {
            if (json == null || json.equalsIgnoreCase("[]") || json.equalsIgnoreCase(NULL)) {
                return;
            }
			json = json.trim();
            if (json.startsWith("[")) {
                JSONArray jsonObject = new JSONArray(json);
				type = TYPE.ARRAY;
                parse(jsonObject);
            } else if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                parse(jsonObject);
            } else {
                Logger.error("Problems parsing JSON item for \n" + json);
                Logger.error("JSONData Stack Trace\n" + StackTraceOutput.getStackTrace(new Throwable(), 5));
				if (json.contains(APPLICATION_ERROR)) {
					throw new JSONDataException(JSONDataException.EXCEPTION_TYPE.SERVER_ERROR);
				}
				throw new JSONDataException(JSONDataException.EXCEPTION_TYPE.INVALID_JSON);
            }
        } catch (JSONException e) {
            Logger.error("Problems parsing JSON item for " + json, e);
			Logger.error("JSONData Stack Trace\n" + StackTraceOutput.getStackTrace(new Throwable(), 5));
			throw new JSONDataException(JSONDataException.EXCEPTION_TYPE.INVALID_JSON, e);
        }
    }

    /**
     * Parse a JSONObject recursively.
     * @param json
     */
    public void parse(JSONObject json) {
        if (json == null) {
            return;
        }
        JSONArray keys = json.names();
        if (keys == null) {
            return;
        }
        int length = keys.length();
        for (int i=0; i < length; i++) {
            try {
                String key = keys.getString(i);
                Object value = json.get(key);
                JSONData jsonData = createJsonData(key, value);
                children.add(jsonData);
            } catch (JSONException e) {
                Logger.error("Problems parsing JSON item for " + name, e);
            }
        }
    }

    /**
     * Create the JSONData object from the key/value
     * @param key
     * @param value
     * @return JSONData
     */
    private JSONData createJsonData(String key, Object value) {
        JSONData jsonData;
        if (value instanceof JSONArray) {
            jsonData = new JSONData(key, null);
			jsonData.type = TYPE.ARRAY;
            jsonData.parse((JSONArray) value);
        } else if (value instanceof JSONObject) {
            jsonData = new JSONData(key, null);
            jsonData.parse((JSONObject) value);
        } else {
            jsonData = new JSONData(key, value);
			jsonData.type = TYPE.VALUE;
        }
        return jsonData;
    }

    /**
     * Parse the json array
     * @param json
     */
    public void parse(JSONArray json) {
        int length = json.length();
        for (int i=0; i < length; i++) {
            try {
                Object child = json.get(i);
                JSONData jsonData = createJsonData(null, child);
                if (jsonData != null) {
                    children.add(jsonData);
                }
            } catch (JSONException e) {
                Logger.error("Problems getting JSON item for " + name, e);
            }
        }
    }

    /**
     * Return the # of items
     * @return size
     */
    public int length() {
        return children.size();
    }

    /**
     * Get the json string value
     * @param item
     * @param name
     * @return string
     */
    protected String getString(JSONObject item, String name) {
        if (item.has(name)) {
            try {
                String value = item.getString(name);
                if (value != null && value.equalsIgnoreCase(NULL)) {
                    return null;
                }
                return value;
            } catch (JSONException e) {
                Logger.error("Problems getting JSON item for " + name, e);
            }
        }
        return null;
    }

    /**
     * Find the children for the given key
     * @param key
     * @return children
     */
    public List<JSONData> findChildren(String key) {
        List<JSONData> foundChildren = new ArrayList<JSONData>();
        searchChildren(this, key, foundChildren);
        return foundChildren;
    }

	/**
	 * Search the children of the given data object on the given key
	 * @param jsonData
	 * @param key
	 * @param children
	 */
    protected void searchChildren(JSONData jsonData, String key, List<JSONData> children) {
		if (jsonData == null || key == null) {
			Logger.error("searchChildren: " + ((jsonData == null) ? "jsonData is null" : "") + ((key == null) ? "key is null" : ""));
			return;
		}
        for (JSONData child : jsonData.children) {
            if (key.equalsIgnoreCase(child.name)) {
                children.add(child);
            }
            searchChildren(child, key, children);
        }
    }

    /**
     * Find a specific child by the given key
     * @param key
     * @return JSONData
     */
    public JSONData findChild(String key) {
		if (key == null) {
			Logger.error("findChild: key is null");
			return null;
		}
        // First search the top level
        for (JSONData child : children) {
            if (key.equalsIgnoreCase(child.name)) {
                return child;
            }
        }
        // Didn't find it, now search the children
        for (JSONData child : children) {
            JSONData data = child.findChild(key);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

	/**
	 * Only search the children of this item. Don't search
	 * it's children
	 * @param key
	 * @return JSONData
	 */
    public JSONData findTopLevelChild(String key) {
        // First search the top level
        for (JSONData child : children) {
            if (key.equalsIgnoreCase(child.name)) {
                return child;
            }
        }
        return null;
    }

	/**
	 * Does this item have a top level child with this key?
	 * @param key
	 * @return true if key found
	 */
    public boolean hasTopLevelChild(String key) {
		return findTopLevelChild(key) != null;
    }

	/**
	 * Convert the class to a JSON compatible string
	 * @return json string
	 */
    @Override
    public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		writeStart(stringBuilder);
		buildString(stringBuilder);
		if (length() > 0 && name != null) {
			writeStart(stringBuilder);
		}
		buildChildString(stringBuilder);
		if (length() > 0 && name != null) {
			writeEnd(stringBuilder);
		}
		writeEnd(stringBuilder);
		return stringBuilder.toString();
	}

	/**
	 * Write the beginning JSON character
	 * @param stringBuilder
	 */
	protected void writeStart(StringBuilder stringBuilder) {
		if (type == TYPE.OBJECT) {
			stringBuilder.append("{");
		} else if (type == TYPE.ARRAY) {
			stringBuilder.append("[");
		}
	}

	/**
	 * Write the ending JSON character
	 * @param stringBuilder
	 */
	protected void writeEnd(StringBuilder stringBuilder) {
		if (type == TYPE.OBJECT) {
			stringBuilder.append("}");
		} else if (type == TYPE.ARRAY) {
			stringBuilder.append("]");
		}
	}

	/**
	 * Build the string from the name/value
	 * @param builder
	 */
	protected void buildString(StringBuilder builder) {
		if (name != null && name.length() > 0) {
			builder.append("\"").append(name).append("\":");
		}
		if (value != null) {
			if (value instanceof Integer || value instanceof Boolean || value == JSONObject.NULL) {
				builder.append(value);
			} else {
				// Put in backspaced quote & backspace
//				String formatted = value.toString().replace("\"", "\\\"");
//				formatted = formatted.replace("\\", "\\\\");
//				builder.append("\"").append(formatted).append("\"");
				buildString(builder, value.toString());
			}
		}
	}

	/**
	 * Taken from JSONStringer. Properly output a string
	 * @param builder
	 * @param value
	 */
	protected void buildString(StringBuilder builder, String value) {
		builder.append("\"");
		for (int i = 0, length = value.length(); i < length; i++) {
			char c = value.charAt(i);

            /*
             * From RFC 4627, "All Unicode characters may be placed within the
             * quotation marks except for the characters that must be escaped:
             * quotation mark, reverse solidus, and the control characters
             * (U+0000 through U+001F)."
             */
			switch (c) {
				case '"':
				case '\\':
				case '/':
					builder.append('\\').append(c);
					break;

				case '\t':
					builder.append("\\t");
					break;

				case '\b':
					builder.append("\\b");
					break;

				case '\n':
					builder.append("\\n");
					break;

				case '\r':
					builder.append("\\r");
					break;

				case '\f':
					builder.append("\\f");
					break;

				default:
					if (c <= 0x1F) {
						builder.append(String.format("\\u%04x", (int) c));
					} else {
						builder.append(c);
					}
					break;
			}

		}
		builder.append("\"");
	}

	/**
	 * Build all child elements
	 * @param builder
	 */
	protected void buildChildString(StringBuilder builder) {
		boolean first = true;
		for (JSONData child : children) {
			if (!first) {
				builder.append(",");
			}
			first = false;
			child.buildString(builder);
			if (child.length() > 0) {
				child.writeStart(builder);
				child.buildChildString(builder);
				child.writeEnd(builder);
			} else if (child.type == TYPE.ARRAY) {
				child.writeStart(builder);
				child.buildChildString(builder);
				child.writeEnd(builder);
			}
		}
	}

	/**
	 * Copy the current JSON object
	 * @return JSONData
	 */
	public JSONData copy() {
		JSONData copy = new JSONData(name, value);
		for (JSONData child : children) {
			copy.add(child.copy());
		}
		return copy;
	}

	/**
	 * Get the children of the current item
	 * @return
	 */
    public List<JSONData> getChildren() {
        return children;
    }

	/**
	 * Get the name of the object
	 * @return name
	 */
    public String getName() {
        return name;
    }

	/**
	 * Get the value of the object
	 * @return value
	 */
    public Object getValue() {
        return value;
    }

    /**
     * Safely get a boolean value
     * @param item
     * @param name
     * @return true/false
     */
    protected boolean getBoolean(JSONObject item, String name) {
        if (item.has(name)) {
            try {
                return item.getBoolean(name);
            } catch (JSONException e) {
                Logger.error("Problems getting JSON item for " + name, e);
            }
        }
        return false;
    }

    /**
     * Safely get in int value. Default to 0 if not there
     * @param item
     * @param name
     * @return int
     */
    protected int getInt(JSONObject item, String name) {
        if (item.has(name)) {
            try {
                return item.getInt(name);
            } catch (JSONException e) {
                Logger.error("Problems getting JSON item for " + name, e);
            }
        }
        return 0;
    }

    public JSONData get(int position) {
        if (position < children.size()) {
            return children.get(position);
        }
        Logger.error("JSONData: get. Invalid position " + position);
        return null;
    }

    public String getChildString(String id) {
        JSONData child = findChild(id);
        if (child == null || isNull(child)) {
            return null;
        }
        if ((child.value instanceof String) && !(NULL.equalsIgnoreCase((String) child.value))) {
            return (String) child.value;
        } else if (child.value != null && (child.value == JSONObject.NULL)) {
            Logger.error("JSONData: getChildString value for " + id + " is null");
        } else if (child.value != null && !(child.value instanceof String)) {
            Logger.error(
				"JSONData: getChildString value for " + id + " is not a string. Type: " + child.value.getClass().toString() + " Value: "
					+ child.value + " for json " + toString());
        }
        return null;
    }

    public boolean has(String id) {
        return findChild(id) != null;
    }

    public boolean getChildBoolean(String id) {
        JSONData child = findChild(id);
        if (child != null && child.value instanceof Boolean) {
            return (Boolean) child.value;
        }
        return false;
    }

    public JSONObject getJsonObject() {
        if (value != null && value instanceof JSONObject) {
            return (JSONObject) value;
        }
		return createJSONObject();
    }

	private JSONObject createJSONObject() {
		try {
			JSONObject jsonObject = new JSONObject();
			if (name != null && value != null) {
				jsonObject.put(name, value);
			}
			for (JSONData child : children) {
				switch (child.type) {
					case OBJECT:
						if (child.name != null) {
							jsonObject.put(child.name, child.createJSONObject());
						}
						break;
					case ARRAY:
						if (child.name != null) {
							jsonObject.put(child.name, child.createJSONArray());
						}
						break;
					case VALUE:
						if (child.name != null && child.value != null) {
							jsonObject.put(child.name, child.value);
						}
						break;

				}
			}
			return jsonObject;
		} catch (JSONException e) {
			Logger.error("JSONData:getJsonObject", e);
		}
		return null;
	}

	protected JSONArray createJSONArray() {
		JSONArray array = new JSONArray();
		if (value != null) {
			array.put(value);
		}
		for (JSONData child : children) {
			switch (child.type) {
				case OBJECT:
					array.put(child.createJSONObject());
					break;
				case ARRAY:
					array.put(child.createJSONArray());
					break;
				case VALUE:
					if (child.value != null) {
						array.put(child.value);
					}
					break;

			}
		}
		return array;
	}

	/**
	 * Get the int with the given id
	 * @param id
	 * @return int
	 */
    public int getChildInt(String id) {
        JSONData child = findChild(id);
        if (child != null && child.value instanceof Integer) {
            return (Integer) child.value;
        }
        return 0;
    }

	/**
	 * Add a Child
	 * @param child
	 */
    public void add(JSONData child) {
		if (child == null) {
			Logger.error("JSONData:Trying to add a null Child");
			return;
		}
        children.add(child);
    }

    /**
     * Add a new set of children
     * @param newChildren
     */
    public void addChildren(List<JSONData> newChildren) {
        children.addAll(newChildren);
    }

    /**
     * Replace the current children with the given list
     * @param newChildren
     */
    public void replaceChildren(List<JSONData> newChildren) {
        children.clear();
        children.addAll(newChildren);
    }

	/**
	 * Update a value for the given id
	 * @param id
	 * @param value
	 */
    public void put(String id, Object value) {
        JSONData child = findChild(id);
        if (child != null) {
            child.value = value;
        } else {
			add(new JSONData(id, value));
        }
    }

    public void remove(JSONData item) {
        children.remove(item);
    }

    public void remove(String id) {
        JSONData child = findChild(id);
        if (child != null) {
            children.remove(child);
        } else {
            Logger.error("JSONData: remove. id " + id + " not found");
        }
    }

    public String getStringValue() {
        if (value != null && value instanceof String) {
            return (String) value;
        }
        return null;
    }

    public int getIntValue() {
        if (value != null && value instanceof Integer) {
            return (Integer) value;
		} else if (value != null && value instanceof Long) {
            return ((Long) value).intValue();
		} else if (value != null && value instanceof Double) {
            return ((Double) value).intValue();
        }
        return 0;
    }

    public boolean getBooleanValue() {
        if (value != null && value instanceof Boolean) {
            return (Boolean) value;
        }
        return false;
    }

    public long getLongValue() {
        if (value != null && value instanceof Long) {
            return (Long) value;
        }
        return 0;
    }

    public double getDoubleValue() {
        if (value != null && value instanceof Double) {
            return (Double) value;
        }
        return 0;
    }

    public boolean isNull(String id) {
        JSONData child = findChild(id);
        if (child == null || child.value == null || child.value == JSONObject.NULL || (child.value instanceof String && (NULL.equalsIgnoreCase((String) child.value)))) {
            return true;
        }
        return false;
    }

    public boolean isNull(JSONData child) {
        if (child == null || child.value == null ||  child.value == JSONObject.NULL || (child.value instanceof String && (NULL.equalsIgnoreCase((String) child.value)))) {
            return true;
        }
        return false;
    }

    public long getChildLong(String id) {
        JSONData child = findChild(id);
        if (child != null && child.value instanceof Long) {
            return (Long) child.value;
        }
        return 0;
    }

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public void setIntValue(int value) {
		this.value = value;
	}

	public void setStringValue(String value) {
		this.value = value;
	}

	public void setBooleanValue(boolean value) {
		this.value = value;
	}

	public void setLongValue(long value) {
		this.value = value;
	}
}

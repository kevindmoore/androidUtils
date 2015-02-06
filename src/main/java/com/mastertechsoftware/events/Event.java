package com.mastertechsoftware.events;

import java.util.HashMap;
/**
 * Class used to pass to other objects through an event bus
 */
public class Event {
	protected String eventType; // These should be unique
	protected String action;// These should be unique
	protected HashMap<String, Object> dataMap = new HashMap<String, Object>();

	public Event(String eventType) {
		this.eventType = eventType;
	}

	public Event(String eventType, String action) {
		this.eventType = eventType;
		this.action = action;
	}

	public void addData(String key, Object data) {
		dataMap.put(key, data);
	}

	public Object getData(String key) {
		return dataMap.get(key);
	}

	/**
	 * Return an int value or -1 if not found
	 * @param key
	 * @return int
	 */
	public int getInt(String key) {
		Object item = dataMap.get(key);
		if (item == null || !(item instanceof Integer)) {
			return -1;
		}
		return (Integer)item;
	}


	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public String toString() {
		return "Event{" +
			"eventType='" + eventType + '\'' +
			", action='" + action + '\'' +
			", dataMap=" + dataMap +
			'}';
	}
}

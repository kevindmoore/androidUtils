package com.mastertechsoftware.util.log;

/**
 * Logging Category
 */
public class Category {
	private boolean enabled = true;
	private String name;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

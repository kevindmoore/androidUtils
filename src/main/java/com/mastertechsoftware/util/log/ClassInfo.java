package com.mastertechsoftware.util.log;

/**
 * Hold Logging Info
 */
public class ClassInfo {
	private boolean enabled = true;
	private String className;
	private String category;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}

package com.mastertechsoftware.notifications;

import android.app.PendingIntent;
/**
 *
 */
public class WearableAction {
	private int icon;
	private String title;
	private PendingIntent pendingIntent;

	public WearableAction(int icon, String title, PendingIntent pendingIntent) {
		this.icon = icon;
		this.title = title;
		this.pendingIntent = pendingIntent;
	}

	public int getIcon() {
		return icon;
	}

	public String getTitle() {
		return title;
	}

	public PendingIntent getPendingIntent() {
		return pendingIntent;
	}
}

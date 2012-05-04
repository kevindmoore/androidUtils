package com.mastertechsoftware.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

/**
 * @author Kevin Moore
 */
public class NotificationHandler {

	protected static int notificationIcon;
	protected static Context context;
	protected static String appName;

	public static void setNotificationIcon(int notificationIcon) {
		NotificationHandler.notificationIcon = notificationIcon;
	}

	public static void setContext(Context context) {
		NotificationHandler.context = context;
	}

	public static void setAppName(String appName) {
		NotificationHandler.appName = appName;
	}

	/**
	 * Add a new Notification
	 * @param notificationTitle
	 * @param msg
	 * @param intent
	 * @param id
	 * @param notificationNum
	 * @param autoCancel
	 */
	public static void addNotification(String notificationTitle, String msg, Intent intent, int id, int notificationNum, boolean autoCancel) {
		NotificationManager mgr = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(notificationIcon,
				notificationTitle,
				System.currentTimeMillis());
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);

		if (autoCancel) {
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
		}
		notification.number = notificationNum;
		notification.setLatestEventInfo(context, appName, msg, pendingIntent);
		mgr.notify(id, notification);
	}

	/**
	 * Start a new notification
	 * @param id
	 * @param notification
	 */
	public static void notify(int id, Notification notification) {
		NotificationManager mgr = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

		mgr.notify(id, notification);
	}

	/**
	 * Cancel a currently running notification.
	 * @param id
	 */
	public static void cancelNotification(int id) {
		NotificationManager mgr = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		mgr.cancel(id);
	}

	/**
	 * Start in foreground and notify
	 * @param service
	 * @param id
	 * @param notification
	 */
	public static void startAndNotify(Service service, int id, Notification notification) {
		if (!startForeground(service, id, notification)) {
			notify(id, notification);
		}
	}

	/**
	 * Stop the foreground process and cancel the notification
	 * @param service
	 * @param id
	 */
	public static void stopAndCancelNotify(Service service, int id) {
		if (!stopForeground(service)) {
			cancelNotification(id);
		}
	}

	/**
	 * Start in the foreground
	 * @param service
	 * @param id
	 * @param notification
	 * @return
	 */
	public static boolean startForeground(Service service, int id, Notification notification) {
        service.startForeground(id, notification);
        return true;
	}

	/**
	 * Stop foreground processing
	 * @param service
	 * @return
	 */
	public static boolean stopForeground(Service service) {
        service.stopForeground(true);
        return true;
	}
}

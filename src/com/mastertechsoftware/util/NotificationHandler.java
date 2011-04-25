package com.mastertechsoftware.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import com.mastertechsoftware.util.reflect.UtilReflector;

import java.lang.reflect.Method;

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

	public static void notify(int id, Notification notification) {
		NotificationManager mgr = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

		mgr.notify(id, notification);
	}
	public static void cancelNotification(int id) {
		NotificationManager mgr = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		mgr.cancel(id);
	}

	public static void startForeground(Service service, int id, Notification notification) {
		Method method = UtilReflector.getMethod(service, "startForeground", new Class[]{int.class, Notification.class});
		if (method != null) {
			UtilReflector.executeMethod(service, new Object[]{id, notification}, method);
		} else {
			service.setForeground(true);
		}
	}

	public static void stopForeground(Service service) {
		Method method = UtilReflector.getMethod(service, "stopForeground", new Class[]{boolean.class});
		if (method != null) {
			UtilReflector.executeMethod(service, new Object[]{true}, method);
		} else {
			service.setForeground(false);
		}
	}
}

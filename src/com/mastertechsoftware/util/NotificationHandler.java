package com.mastertechsoftware.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

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
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
//		Notification notification = new Notification(notificationIcon,
//				notificationTitle,
//				System.currentTimeMillis());
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);

		builder.setSmallIcon(notificationIcon).setContentText(msg).setNumber(notificationNum).setContentTitle(notificationTitle).
			setContentIntent(pendingIntent);
		Notification notification = builder.build();
		if (autoCancel) {
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
		}
//		notification.number = notificationNum;
//		notification.setLatestEventInfo(context, appName, msg, pendingIntent);
		mgr.notify(id, notification);
	}

	/**
	 * Create a notification given a title & msg.
	 * @param notificationTitle
	 * @param msg
	 * @return Notification
	 */
	public static Notification createNotification(String notificationTitle, String msg) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

		builder.setSmallIcon(notificationIcon).setContentText(msg).setContentTitle(notificationTitle);
		Notification notification = builder.build();
		return notification;
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
	 * Now that we're using the newer SDK, we can always call this
	 * @param service
	 * @param id
	 * @param notification
	 * @return true - used this
	 */
	public static boolean startForeground(Service service, int id, Notification notification) {
        service.startForeground(id, notification);
        return true;
	}

	/**
	 * Stop foreground processing
	 * Now that we're using the newer SDK, we can always call this
	 * @param service
	 * @return true - used this
	 */
	public static boolean stopForeground(Service service) {
        service.stopForeground(true);
        return true;
	}
}

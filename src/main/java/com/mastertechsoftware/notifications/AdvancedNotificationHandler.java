package com.mastertechsoftware.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
/**
 *  Use new notification code
 */
public class AdvancedNotificationHandler {
    protected static Context context;


    public static void setContext(Context context) {
        AdvancedNotificationHandler.context = context;
    }

    public static void addNotification(String notificationTitle, String msg, String wearableTitle, int notificationIcon, PendingIntent intent, int id, boolean autoCancel) {
        NotificationCompat.Builder deviceNotification = new NotificationCompat.Builder(context);
        deviceNotification
            .setAutoCancel(autoCancel)
            .setContentTitle(notificationTitle)
            .setContentText(msg)
            .setSmallIcon(notificationIcon)
            .setContentIntent(intent);
        NotificationCompat.Action.Builder wearActionBuilder=
            new NotificationCompat.Action.Builder(notificationIcon,
                                                  wearableTitle,
                                                  intent);

        NotificationCompat.Builder extended=
            new NotificationCompat.WearableExtender()
                .addAction(wearActionBuilder.build())
                .extend(deviceNotification);

        NotificationManagerCompat mgr =
            NotificationManagerCompat.from(context);

        mgr.notify(id, extended.build());
    }

    public static void addNotification(NotificationCompat.Builder builder, int id) {
        NotificationManagerCompat mgr =
            NotificationManagerCompat.from(context);

        mgr.notify(id, builder.build());
    }

    public static void addNotification(Notification notification, int id) {
        NotificationManagerCompat mgr =
            NotificationManagerCompat.from(context);

        mgr.notify(id, notification);
    }

    public static void cancelNotification(int id) {
        NotificationManagerCompat mgr =
            NotificationManagerCompat.from(context);

        mgr.cancel(id);

    }
}

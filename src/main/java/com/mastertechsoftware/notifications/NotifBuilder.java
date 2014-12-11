package com.mastertechsoftware.notifications;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;
/**
 *  Builder for Notifications
 */
public class NotifBuilder {
    protected PendingIntent phoneIntent;
    protected PendingIntent wearIntent;
    protected String phoneTitle;
    protected String msg;
    protected String wearableTitle;
    protected int phoneIcon;
    protected int wearIcon;
    protected int id;
    protected boolean autoCancel;
    protected boolean localOnly = false;
    protected boolean ongoing = false;
    protected TaskStackBuilder stackBuilder;
    private NotificationCompat.WearableExtender wearableExtender;
    protected List<NotificationCompat.Action.Builder> wearableActions = new ArrayList<NotificationCompat.Action.Builder>();
    protected List<NotificationCompat.Builder> wearablePages = new ArrayList<NotificationCompat.Builder>();

    public NotifBuilder setPhoneIntent(PendingIntent phoneIntent) {
        this.phoneIntent = phoneIntent;
        return this;
    }
    public NotifBuilder setWearIntent(PendingIntent wearIntent) {
        this.wearIntent = wearIntent;
        return this;
    }
    public NotifBuilder setPhoneTitle(String phoneTitle) {
        this.phoneTitle = phoneTitle;
        return this;
    }

    public NotifBuilder setWearTitle(String wearableTitle) {
        this.wearableTitle = wearableTitle;
        return this;
    }

    public NotifBuilder setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public NotifBuilder setPhoneIcon(int phoneIcon) {
        this.phoneIcon = phoneIcon;
        return this;
    }

    public NotifBuilder setWearIcon(int wearIcon) {
        this.wearIcon = wearIcon;
        return this;
    }

    public NotifBuilder setId(int id) {
        this.id = id;
        return this;
    }

    public NotifBuilder setLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
        return this;
    }

    public NotifBuilder setAutoCancel(boolean autoCancel) {
        this.autoCancel = autoCancel;
        return this;
    }

    public NotifBuilder setOngoing(boolean ongoing) {
        this.ongoing = ongoing;
        return this;
    }

    public NotifBuilder addBackStack(Context context, Class backClass) {
        if (stackBuilder == null) {
            stackBuilder = TaskStackBuilder.create(context);
        }
        stackBuilder.addParentStack(backClass);
        stackBuilder.addNextIntent(new Intent(context, backClass));
        return this;
    }

    public NotifBuilder addCurrentWearableAction() {
        wearableActions.add(createBuilder(wearIcon, wearableTitle, wearIntent));
        return this;
    }

    public NotifBuilder addWearableAction(int icon, String title, PendingIntent intent) {
        wearableActions.add(createBuilder(icon, title, intent));
        return this;
    }

    public NotifBuilder addWearablePage(Context context, int icon, String title, PendingIntent intent) {
        wearablePages.add(createBuilder(context, icon, title, intent));
        return this;
    }

    public NotificationCompat.WearableExtender getWearableExtender() {
        return wearableExtender;
    }


    public NotificationCompat.Builder build(Context context) {
        NotificationCompat.Builder deviceNotification = new NotificationCompat.Builder(context);
        deviceNotification.setAutoCancel(autoCancel);
        deviceNotification.setOngoing(ongoing);
        deviceNotification.setLocalOnly(localOnly);
        if (phoneTitle != null) {
            deviceNotification.setContentTitle(phoneTitle);
        }
        if (msg != null) {
            deviceNotification.setContentText(msg);
        }
        if (phoneIcon != 0) {
            deviceNotification.setSmallIcon(phoneIcon);
        }
        if (stackBuilder != null) {
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            deviceNotification.setContentIntent(pendingIntent);
        } else if (phoneIntent != null) {
            deviceNotification.setContentIntent(phoneIntent);
        }

        if (wearableActions.size() > 0) {
            wearableExtender = new NotificationCompat.WearableExtender();
            for (NotificationCompat.Action.Builder wearableAction : wearableActions) {
                wearableExtender.addAction(wearableAction.build());
            }
            for (NotificationCompat.Builder wearablePage : wearablePages) {
                wearableExtender.addPage(wearablePage.build());
            }
//            return deviceNotification.extend(wearableExtender);
            return wearableExtender.extend(deviceNotification);
//        }
//        NotificationCompat.Action.Builder wearActionBuilder = null;
//        if (wearIcon != 0 || wearableTitle != null || wearIntent != null) {
//            wearActionBuilder = createBuilder(wearIcon != 0 ? wearIcon : phoneIcon, wearableTitle != null ? wearableTitle : phoneTitle,
//                                              wearIntent != null ? wearIntent : phoneIntent);
//            wearableExtender = new NotificationCompat.WearableExtender();
//            wearableExtender.addAction(wearActionBuilder.build());
//            return wearableExtender.extend(deviceNotification);
        } else {
            return deviceNotification;
        }
    }

    public NotificationCompat.Action.Builder createBuilder(int icon, String title, PendingIntent intent) {
        return new NotificationCompat.Action.Builder(icon, title, intent);
    }

    public NotificationCompat.Builder createBuilder(Context context, int icon, String title, PendingIntent intent) {
//        NotificationCompat.BigPictureStyle builder = new NotificationCompat.BigPictureStyle();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
//        builder.setBigContentTitle(title);
//        builder.setSmallIcon(icon);
//        builder.set(intent);
        builder.setContentTitle(title);
        builder.setSmallIcon(icon);
        builder.setContentIntent(intent);
        return builder;
    }
}

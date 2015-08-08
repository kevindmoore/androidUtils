package com.mastertechsoftware.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

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
    protected boolean vibrate = false;
    protected TaskStackBuilder stackBuilder;
    protected NotificationCompat.WearableExtender wearableExtender;
    protected List<NotificationCompat.Action.Builder> wearableActions = new ArrayList<NotificationCompat.Action.Builder>();
    protected List<Notification.Action.Builder> wearableLollipopActions = new ArrayList<Notification.Action.Builder>();
    protected List<Notification.Action.Builder> actions = new ArrayList<Notification.Action.Builder>();
    protected List<NotificationCompat.Builder> wearablePages = new ArrayList<NotificationCompat.Builder>();
    protected Notification.Style style;
    protected Bitmap largeIcon;

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

    public NotifBuilder setLargeBitmap(Bitmap largeIcon) {
        this.largeIcon = largeIcon;
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

	public NotifBuilder setVibrating(boolean vibrate) {
		this.vibrate = vibrate;
		return this;
	}

	public NotifBuilder setStyle(Notification.Style style) {
		this.style = style;
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

    public NotifBuilder addWearableAction(int icon, String title, PendingIntent intent) {
        wearableActions.add(createBuilder(icon, title, intent));
        return this;
    }

    public NotifBuilder addWearableAction(WearableAction wearableAction) {
        wearableActions.add(createBuilder(wearableAction.getIcon(), wearableAction.getTitle(), wearableAction.getPendingIntent()));
        return this;
    }

    public NotifBuilder addLollipopWearableAction(int icon, String title, PendingIntent intent) {
        wearableLollipopActions.add(createLollipopBuilder(icon, title, intent));
        return this;
    }

    public NotifBuilder addLollipopWearableAction(WearableAction wearableAction) {
        wearableLollipopActions.add(createLollipopBuilder(wearableAction.getIcon(), wearableAction.getTitle(),
                                                          wearableAction.getPendingIntent()));
        return this;
    }

    public NotifBuilder addAction(WearableAction wearableAction) {
        actions.add(createLollipopBuilder(wearableAction.getIcon(), wearableAction.getTitle(), wearableAction.getPendingIntent()));
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
        deviceNotification.setAutoCancel(autoCancel).setOngoing(ongoing).setLocalOnly(localOnly);
		if (vibrate) {
			deviceNotification.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
		}
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

        deviceNotification.setShowWhen(false);

        if (wearableActions.size() > 0) {
            wearableExtender = new NotificationCompat.WearableExtender();
            for (NotificationCompat.Action.Builder wearableAction : wearableActions) {
                wearableExtender.addAction(wearableAction.build());
            }
            for (NotificationCompat.Builder wearablePage : wearablePages) {
                wearableExtender.addPage(wearablePage.build());
            }
            return deviceNotification.extend(wearableExtender);
//            return wearableExtender.extend(deviceNotification);
//        }
//        NotificationCompat.Action.Builder wearActionBuilder = null;
//        if (wearIcon != 0 || wearableTitle != null || wearIntent != null) {
//            wearActionBuilder = createBuilder(wearIcon != 0 ? wearIcon : phoneIcon, wearableTitle != null ? wearableTitle : phoneTitle,
//                                              wearIntent != null ? wearIntent : phoneIntent);
//            wearableExtender = new NotificationCompat.WearableExtender();
//            wearableExtender.addAction(wearActionBuilder.build());
//            return wearableExtender.extend(deviceNotification);
        } else {
			if (wearIcon != 0 && wearableTitle != null && wearIntent != null) {
				wearableExtender = new NotificationCompat.WearableExtender();
				wearableExtender.addAction(createBuilder(wearIcon, wearableTitle, wearIntent).build());
				return deviceNotification.extend(wearableExtender);
			}
            return deviceNotification;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Notification.Builder buildLollipop(Context context) {
        Notification.Builder deviceNotification = new Notification.Builder(context);
        deviceNotification.setAutoCancel(autoCancel).setOngoing(ongoing).setLocalOnly(localOnly);
		if (vibrate) {
			deviceNotification.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
		}
        if (phoneTitle != null) {
            deviceNotification.setContentTitle(phoneTitle);
        }
        if (style != null) {
            deviceNotification.setStyle(style);
        }
        if (msg != null) {
            deviceNotification.setContentText(msg);
        }
        if (phoneIcon != 0) {
            deviceNotification.setSmallIcon(phoneIcon);
        }
        if (largeIcon != null) {
            deviceNotification.setLargeIcon(largeIcon);
        }
        deviceNotification.setVisibility(Notification.VISIBILITY_PUBLIC);
        if (stackBuilder != null) {
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            deviceNotification.setContentIntent(pendingIntent);
        } else if (phoneIntent != null) {
            deviceNotification.setContentIntent(phoneIntent);
        }

        deviceNotification.setShowWhen(false);
        if (actions.size() > 0) {
            for (Notification.Action.Builder action : actions) {
                deviceNotification.addAction(action.build());
            }
        }
        Notification.WearableExtender wearableExtender;
        if (wearableActions.size() > 0) {
            wearableExtender = new Notification.WearableExtender();
            for (Notification.Action.Builder wearableAction : wearableLollipopActions) {
                wearableExtender.addAction(wearableAction.build());
            }
            for (NotificationCompat.Builder wearablePage : wearablePages) {
                wearableExtender.addPage(wearablePage.build());
            }
            return deviceNotification.extend(wearableExtender);
//            return wearableExtender.extend(deviceNotification);
//        }
//        Notification.Action.Builder wearActionBuilder = null;
//        if (wearIcon != 0 || wearableTitle != null || wearIntent != null) {
//            wearActionBuilder = createBuilder(wearIcon != 0 ? wearIcon : phoneIcon, wearableTitle != null ? wearableTitle : phoneTitle,
//                                              wearIntent != null ? wearIntent : phoneIntent);
//            wearableExtender = new Notification.WearableExtender();
//            wearableExtender.addAction(wearActionBuilder.build());
//            return wearableExtender.extend(deviceNotification);
        } else {
			if (wearIcon != 0 && wearableTitle != null && wearIntent != null) {
				wearableExtender = new Notification.WearableExtender();
				wearableExtender.addAction(createLollipopBuilder(wearIcon, wearableTitle, wearIntent).build());
				return deviceNotification.extend(wearableExtender);
			}
            return deviceNotification;
        }
    }

    public NotificationCompat.Action.Builder createBuilder(int icon, String title, PendingIntent intent) {
        return new NotificationCompat.Action.Builder(icon, title, intent);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    public Notification.Action.Builder createLollipopBuilder(int icon, String title, PendingIntent intent) {
        return new Notification.Action.Builder(icon, title, intent);
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

    public Notification.Builder createLollipopBuilder(Context context, int icon, String title, PendingIntent intent) {
//        NotificationCompat.BigPictureStyle builder = new NotificationCompat.BigPictureStyle();
        Notification.Builder builder = new Notification.Builder(context);
//        builder.setBigContentTitle(title);
//        builder.setSmallIcon(icon);
//        builder.set(intent);
        builder.setContentTitle(title);
        builder.setSmallIcon(icon);
        builder.setContentIntent(intent);
        return builder;
    }
}

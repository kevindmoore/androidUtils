package com.mastertechsoftware.util;

import android.content.Context;
import android.os.PowerManager;

public class PowerLock {
	private static PowerManager.WakeLock wakeLock;

	public static void getWakeLock(Context context) {
 		releaseWakeLock();
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "aMusicPlayer");
		wakeLock.acquire();		
	}
	
	public static void releaseWakeLock() {
		if (wakeLock != null) {
			wakeLock.release();
		}
		wakeLock = null;
	}
}

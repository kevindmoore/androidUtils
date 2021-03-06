package com.mastertechsoftware.util;

import android.content.Context;
import android.os.PowerManager;
import com.mastertechsoftware.util.log.Logger;

public class PowerLock {
	private static PowerManager.WakeLock wakeLock;

	public static void getWakeLock(Context context) {
		if (context == null) {
			Logger.error("PowerLock:getWakeLock: Context is null");
			return;
		}
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

package com.mastertechsoftware.util;

import android.content.Context;
import android.net.wifi.WifiManager;
import com.mastertechsoftware.network.NetworkManager;
import com.mastertechsoftware.util.log.Logger;

/**
 * @author Kevin Moore
 */
public class LockManager {
	protected static WifiManager.WifiLock wifiLock;
	protected static Context context;
	private static LockManager instance;
	private static boolean wifiLockCreated = false;
	private static boolean lockCreated = false;
	private static boolean debugging = false;

	public static LockManager getInstance() {
		if (instance == null) {
			instance = new LockManager();
		}
		return instance;
	}

	private LockManager() {
		Logger.setDebug(this.getClass().getSimpleName(), debugging);
	}

	public static void setContext(Context context) {
		LockManager.context = context;
	}

	/**
	 * NOTE: This has to not be called on the UI Thread
	 * Create locks
	 */
	public static void startLocks() {
		createWifiLock();

		// Make sure our connection is working
		NetworkManager.primeInternet();

		createWakeLock();
	}

	public static void createWakeLock() {
		if (!lockCreated) {
//			Logger.debugLocal(LockManager.class.getSimpleName(), "Creating lock");
			PowerLock.getWakeLock(context);
			lockCreated = true;
		}
	}

	public static void createWifiLock() {
		if (NetworkManager.wifiIsActive() && !wifiLockCreated) {
//			Logger.debugLocal(LockManager.class.getSimpleName(), "Running on wifi - Creating wifi lock");
			wifiLock = NetworkManager.getWifiLock();
			wifiLockCreated = true;
		}
	}

	public static void	endLocks() {
		releaseWifiLock();
		releaseWakeLock();
	}

	public static void releaseWakeLock() {
		if (lockCreated) {
			PowerLock.releaseWakeLock();
//			Logger.debugLocal(LockManager.class.getSimpleName(), "Releasing lock");
		}
		lockCreated = false;
	}

	public static void releaseWifiLock() {
		if (wifiLock != null) {
//			Logger.debugLocal(LockManager.class.getSimpleName(), "Releasing wifi lock");
			NetworkManager.releaseWifiLock(wifiLock);
		}
		wifiLockCreated = false;
		wifiLock = null;
	}

	public static WifiManager.WifiLock getWifiLock() {
		return wifiLock;
	}

	public static boolean hasWifiLock() {
		return wifiLockCreated;
	}

	public static boolean isLockCreated() {
		return lockCreated;
	}

	public static boolean isWifiLockCreated() {
		return wifiLockCreated;
	}
}

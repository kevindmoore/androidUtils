package com.mastertechsoftware.util;

import android.content.Context;
import android.net.wifi.WifiManager;
import com.mastertechsoftware.network.NetworkManager;

/**
 * @author Kevin Moore
 */
public class LockManager {
	protected static WifiManager.WifiLock wifiLock;
	protected static Context context;
	private static LockManager instance;
	private static boolean wifiLockCreated = false;
	private static boolean lockCreated = false;

	public static LockManager getInstance() {
		if (instance == null) {
			instance = new LockManager();
		}
		return instance;
	}

	private LockManager() {
	}

	public static void setContext(Context context) {
		LockManager.context = context;
	}

	public static void startLocks() {
		createWifiLock();

		// Make sure our connection is working
		NetworkManager.primeInternet();

		if (!lockCreated) {
			Logger.debug("Creating lock");
			PowerLock.getWakeLock(context);
			lockCreated = true;
		}
	}

	public static void createWifiLock() {
		if (NetworkManager.wifiIsActive() && !wifiLockCreated) {
			Logger.debug("Running on wifi - Creating wifi lock");
			wifiLock = NetworkManager.getWifiLock();
			wifiLockCreated = true;
		}
	}

	public static void	endLocks() {
		releaseWifiLock();
		if (lockCreated) {
			PowerLock.releaseWakeLock();
			Logger.debug("Releasing lock");
		}
		lockCreated = false;
	}

	public static void releaseWifiLock() {
		if (wifiLock != null) {
			Logger.debug("Releasing wifi lock");
			NetworkManager.releaseWifiLock(wifiLock);
		}
		wifiLockCreated = false;
		wifiLock = null;
	}

	public static WifiManager.WifiLock getWifiLock() {
		return wifiLock;
	}

	public static boolean hasWifiLock() {
		return wifiLock != null;
	}
}

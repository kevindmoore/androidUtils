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

		PowerLock.getWakeLock(context);
	}

	public static void createWifiLock() {
		if (NetworkManager.wifiIsActive()) {
			Logger.debug("Running on wifi");
			wifiLock = NetworkManager.getWifiLock();
		}
	}

	public static void	endLocks() {
		releaseWifiLock();
		PowerLock.releaseWakeLock();
	}

	public static void releaseWifiLock() {
		if (wifiLock != null) {
			Logger.debug("Releasing wifi lock");
			NetworkManager.releaseWifiLock(wifiLock);
		}
		wifiLock = null;
	}

	public static WifiManager.WifiLock getWifiLock() {
		return wifiLock;
	}

	public static boolean hasWifiLock() {
		return wifiLock != null;
	}
}

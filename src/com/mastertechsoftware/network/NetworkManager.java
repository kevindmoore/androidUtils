package com.mastertechsoftware.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.mastertechsoftware.util.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * @author Kevin Moore
 *         Copyright (c) 1996-2010 by Cisco Systems, Inc.
 *         All rights reserved. Cisco confidential
 */
public class NetworkManager {
	public enum State {
		UNKNOWN,

		/**
		 * This state is returned if there is connectivity to any network *
		 */
		CONNECTED,
		/**
		 * This state is returned if there is no connectivity to any network. This is set to true
		 * under two circumstances:
		 * <ul>
		 * <li>When connectivity is lost to one network, and there is no other available network to
		 * attempt to switch to.</li>
		 * <li>When connectivity is lost to one network, and the attempt to switch to another
		 * network fails.</li>
		 */
		NOT_CONNECTED
	}

	public enum NetworkType {
		// Regular mobile
		MOBILE,

		// Wifi
		WIFI
	}

	private static NetworkManager instance;
	private static State mState = State.UNKNOWN;
	private static State mCurrentNetworkState = State.UNKNOWN;
	private static NetworkType mNetworkType = NetworkType.MOBILE;
	private static boolean connectionBroken = false;
	private static ConnectivityBroadcastReceiver mReceiver = new ConnectivityBroadcastReceiver();
	private static final boolean DBG = true;

	private static Context mContext;

	private static HashMap<Handler, Integer> mHandlers = new HashMap<Handler, Integer>();


	private static boolean mListening;

	private static String mReason;

	private static boolean mIsFailover;

	/**
	 * Network connectivity information
	 */
	private static NetworkInfo mNetworkInfo;

	/**
	 * In case of a Disconnect, the connectivity manager may have already established, or may be
	 * attempting to establish, connectivity with another network. If so, {@code mOtherNetworkInfo}
	 * will be non-null.
	 */
	private static NetworkInfo mOtherNetworkInfo;


	/**
	 * Singleton. Only want 1 instance since we don't want a lot of instances with registered handlers
	 * @return NetworkManager
	 */
	public static NetworkManager getInstance() {
		if (instance == null) {
			instance = new NetworkManager();
		}
		return instance;
	}

	/**
	 * Set the context 1x - from application or an activity
	 * @param mContext
	 */
	public static void setContext(Context mContext) {
		NetworkManager.mContext = mContext;
	}

	private NetworkManager() {
	}

	/**
	 * Checks the network connection.
	 *
	 * @return true if there is an active network, false otherwise.
	 */
	public static boolean hasActiveNetwork() {
		boolean result = false;

		ConnectivityManager mgr = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = mgr.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isAvailable() && netInfo.isConnected()) {
			result = true;
		}

		return result;
	}

	/**
	 * Get if wifi is active
	 * @return
	 */
	public static boolean wifiIsActive() {
		WifiManager mgr = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		boolean active = false;
		if (mgr.isWifiEnabled()) {
			WifiInfo connectionInfo = mgr.getConnectionInfo();
			if (connectionInfo != null && connectionInfo.getBSSID() != null ) {
				active = true;
			}
		}
		return active;
	}

	/**
	 * Get a new wifi lock
	 * @return  WifiLock
	 */
	public static WifiManager.WifiLock getWifiLock() {
		WifiManager mgr = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		WifiManager.WifiLock wifiLock = mgr.createWifiLock(WifiManager.WIFI_MODE_FULL, mContext.getClass().toString());
		wifiLock.acquire();
		return wifiLock;
	}

	/**
	 * Release a specific lock
	 * @param lock
	 */
	public static void releaseWifiLock(WifiManager.WifiLock lock) {
		lock.release();
	}

	/**
	 * Make a call to the internet. Should decrease the chance of an UnknownHostException
	 */
	public static void primeInternet() {
		try {
			InetAddress i = InetAddress.getByName("http://www.google.com");
		} catch (UnknownHostException e1) {
		}
	}



/*
 * Copyright (C) 2006 The Android Open Source Project Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */

	private static class ConnectivityBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION) || !mListening) {
				Logger.debug("onReceived() called with " + mState.toString() + " and " + intent);
				return;
			}

			boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

			if (noConnectivity) {
				mState = State.NOT_CONNECTED;
				connectionBroken = true;
			} else {
				mState = State.CONNECTED;
			}

			mNetworkInfo = (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			mOtherNetworkInfo = (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

			mReason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
			mIsFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

			if (DBG) {
				Logger.debug("onReceive(): mNetworkInfo=" + mNetworkInfo + " mOtherNetworkInfo = "
						+ (mOtherNetworkInfo == null ? "[none]" : mOtherNetworkInfo) + " noConn=" + noConnectivity
						+ " mState=" + mState.toString());
			}

			Bundle data = new Bundle();
			// Need to send msg about current network
			mNetworkType = getNetworkType(mNetworkInfo.getType());
			mCurrentNetworkState = getNetworkState(mNetworkInfo.getState());
			data.putInt("state", mCurrentNetworkState.ordinal());
			data.putInt("type", mNetworkType.ordinal());
			Logger.debug("Sending Network Message " + mCurrentNetworkState.ordinal());
			// Notifiy any handlers.
			for (Handler handler : mHandlers.keySet()) {
				Message message = Message.obtain(handler, mHandlers.get(handler));
				message.setData(data);
				handler.sendMessage(message);
			}
		}
	}


	/**
	 * Give a system network type, we want just a simple mobile or wifi
	 * @param systemType
	 * @return NetworkType
	 */
	public static NetworkType getNetworkType(int systemType) {
		switch (systemType) {
			case ConnectivityManager.TYPE_WIFI:
				return NetworkType.WIFI;
			case ConnectivityManager.TYPE_MOBILE:
				return NetworkType.MOBILE;

			default:
				return NetworkType.MOBILE;

		}
	}

	public static State getNetworkState(NetworkInfo.State systemState) {
		if (systemState.equals(NetworkInfo.State.CONNECTED)) {
				return State.CONNECTED;
		} else if (systemState.equals(NetworkInfo.State.DISCONNECTING)) {
				return State.NOT_CONNECTED;
		} else if (systemState.equals(NetworkInfo.State.DISCONNECTED)) {
				return State.NOT_CONNECTED;
		} else if (systemState.equals(NetworkInfo.State.UNKNOWN)) {
				return State.NOT_CONNECTED;
		} else if (systemState.equals(NetworkInfo.State.SUSPENDED)) {
				return State.NOT_CONNECTED;
		}

		return State.CONNECTED;

	}

	public static void clearConnectionFlag() {
		connectionBroken = false;
	}

	public static boolean connectionBroken() {
		return connectionBroken;
	}

	/**
	 * This method starts listening for network connectivity state changes.
	 *
	 */
	public static synchronized void startListening() {
		if (!mListening) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			Logger.debug("NetworkManager: startListening");
			mContext.registerReceiver(mReceiver, filter);
			mListening = true;
		}
	}

	/**
	 * This method stops this class from listening for network changes.
	 */
	public static synchronized void stopListening() {
		if (mListening) {
			mContext.unregisterReceiver(mReceiver);
			mNetworkInfo = null;
			mOtherNetworkInfo = null;
			mIsFailover = false;
			mReason = null;
			mListening = false;
		}
	}

	/**
	 * This methods registers a Handler to be called back onto with the specified what code when the
	 * network connectivity state changes.
	 *
	 * @param target The target handler.
	 * @param what   The what code to be used when posting a message to the handler.
	 */
	public static void registerHandler(Handler target, int what) {
		mHandlers.put(target, what);
	}

	/**
	 * This methods unregisters the specified Handler.
	 *
	 * @param target
	 */
	public static void unregisterHandler(Handler target) {
		mHandlers.remove(target);
	}

	/**
	 * Get the latest state
	 * @return
	 */
	public static State getState() {
		return mState;
	}

	/**
	 * Return the NetworkInfo associated with the most recent connectivity event.
	 *
	 * @return {@code NetworkInfo} for the network that had the most recent connectivity event.
	 */
	public static NetworkInfo getNetworkInfo() {
		return mNetworkInfo;
	}

	/**
	 * If the most recent connectivity event was a DISCONNECT, return any information supplied in
	 * the broadcast about an alternate network that might be available. If this returns a non-null
	 * value, then another broadcast should follow shortly indicating whether connection to the
	 * other network succeeded.
	 *
	 * @return NetworkInfo
	 */
	public static NetworkInfo getOtherNetworkInfo() {
		return mOtherNetworkInfo;
	}

	/**
	 * Returns true if the most recent event was for an attempt to switch over to a new network
	 * following loss of connectivity on another network.
	 *
	 * @return {@code true} if this was a failover attempt, {@code false} otherwise.
	 */
	public static boolean isFailover() {
		return mIsFailover;
	}

	/**
	 * An optional reason for the connectivity state change may have been supplied. This returns it.
	 *
	 * @return the reason for the state change, if available, or {@code null} otherwise.
	 */
	public static String getReason() {
		return mReason;
	}
}

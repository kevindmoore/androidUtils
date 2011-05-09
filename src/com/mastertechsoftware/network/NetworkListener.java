package com.mastertechsoftware.network;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.mastertechsoftware.thread.LooperThread;
import com.mastertechsoftware.thread.ThreadWait;
import com.mastertechsoftware.util.LockManager;
import com.mastertechsoftware.util.Logger;

/**
 * @author Kevin Moore
 */
public class NetworkListener {

	protected ThreadWait threadWait;
	private LooperThread handlerThread;
	private Context mContext;
	boolean hasWifiLock = false;
	boolean wifiReleased = false;

	public NetworkListener(Context mContext, ThreadWait threadWait) {
		this.mContext = mContext;
		this.threadWait = threadWait;
	}


	public void startListening() {
		hasWifiLock = NetworkManager.wifiIsActive() && LockManager.hasWifiLock();
		Logger.error("NetworkListener " + (hasWifiLock ? " has wifi lock" : " does not have wifi lock"));
		handlerThread = new LooperThread(new LooperThread.LooperCallback() {
			@Override
			public boolean handleMessage(Message msg) {
				Logger.error("NetworkListener msg received");
				Bundle data = msg.getData();
				if (data != null) {
					Logger.error("NetworkListener found data");
					int state = data.getInt("state");
					Logger.error("NetworkListener state=" + state);
					if (threadWait.isWaiting() && state == NetworkManager.State.CONNECTED.ordinal()) {
						Logger.error("Network Connected");
						if (hasWifiLock && wifiReleased) {
							if (NetworkManager.wifiIsActive()) {
								Logger.error("Creating wifi lock");
								LockManager.createWifiLock();
								wifiReleased = false;
							}
						}
						threadWait.resume(); // Wake up thread
					} else if (state == NetworkManager.State.NOT_CONNECTED.ordinal()) {
						Logger.error("Network Disconnected");
						if (hasWifiLock) {
							if (!NetworkManager.wifiIsActive()) {
								Logger.error("Releasing wifi lock");
								LockManager.releaseWifiLock();
								wifiReleased = true;
							}
						}
					}
					return true;
				}
				return false;
			}

			@Override
			public void setHandler(Handler handler) {
				Logger.error("NetworkListener setHandler");
				NetworkManager.registerHandler(handler, 0);
				NetworkManager.startListening();
			}
		});
		handlerThread.start();
	}

	public void stopListening() {
		NetworkManager.stopListening();
		NetworkManager.unregisterHandler(handlerThread.getHandler());
		handlerThread.quit();
	}
}

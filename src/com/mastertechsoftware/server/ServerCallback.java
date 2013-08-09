package com.mastertechsoftware.server;

/**
 * Callback for server calls
 */
public interface ServerCallback {
	void onSuccess(ServerResponse response);
	void onFailure(ServerResponse error);
}

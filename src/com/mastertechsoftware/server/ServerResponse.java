package com.mastertechsoftware.server;

/**
 * On a successful server call, return the response
 * There are other 200-300 code responses that are still valid
 */
public class ServerResponse {
	protected String response;
	protected int responseCode;
	protected boolean success = true;

	public ServerResponse(String response, int responseCode) {
		this.response = response;
		this.responseCode = responseCode;
	}

	public ServerResponse(String response, int responseCode, boolean success) {
		this.response = response;
		this.responseCode = responseCode;
		this.success = success;
	}

	public String getResponse() {
		return response;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	@Override
	public String toString() {
		return "Response code " + responseCode + " Response: " + response;
	}
}

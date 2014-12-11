package com.mastertechsoftware.server;

import org.apache.http.HttpEntity;

import java.util.List;
import java.util.Map;
/**
 * Hold the parameters needed for a server call
 */
public class ServerParams {
	protected List<String> urlParms;
	protected String postData;
	protected Map<String, String> params;
	protected ServerCallback callback;
	protected HttpEntity entity;

	public ServerParams(List<String> urlParms, ServerCallback callback) {
		this.urlParms = urlParms;
		this.callback = callback;
	}

	public ServerParams(String postData, Map<String, String> params, ServerCallback callback) {
		this.postData = postData;
		this.params = params;
		this.callback = callback;
	}

	public List<String> getUrlParms() {
		return urlParms;
	}

	public String getPostData() {
		return postData;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public ServerCallback getCallback() {
		return callback;
	}

	public HttpEntity getEntity() {
		return entity;
	}

	public void setEntity(HttpEntity entity) {
		this.entity = entity;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (urlParms != null) {
			builder.append("urlParms ").append(urlParms.toString());
		}
		if (postData != null) {
			builder.append("postData ").append(postData);
		}
		if (params != null) {
			builder.append("params ").append(params);
		}
		return builder.toString();
	}

}

package com.mastertechsoftware.server;

import com.mastertechsoftware.stream.StreamType;
/**
 * Class used to hold information on a server call
 */
public class ServerCall {
	protected ServerCallName name;
	protected String url;
	protected StreamType type;

	public ServerCall(StreamType type, String url) {
		this.type = type;
		this.url = url;
	}

	public ServerCall(ServerCallName name, StreamType type, String url) {
		this.name = name;
		this.type = type;
		this.url = url;
	}

	public ServerCallName getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public StreamType getType() {
		return type;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (name != null) {
			builder.append("Call ").append(name);
		}
		if (url != null) {
			builder.append("url ").append(url);
		}
		if (type != null) {
			builder.append("type ").append(type);
		}
		return builder.toString();
	}

}

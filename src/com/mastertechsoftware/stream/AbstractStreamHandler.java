package com.mastertechsoftware.stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

/**
 * @author Kevin Moore
 */
public class AbstractStreamHandler<Result> implements StreamHandler<Result> {
	protected StreamProcessor<Result> processor;

	public int getType() {
		return GET_TYPE;
	}

	public Result processInputStream(InputStream stream) throws StreamException {
		return null;
	}

	public void writeOutputStream(OutputStream stream) throws StreamException {

	}

	public void setupConnection(URLConnection connection) {

	}

	public void setStreamProcessor(StreamProcessor<Result> processor) {
		this.processor = processor;
	}

	public StreamProcessor<Result> getProcessor() {
		return processor;
	}
}

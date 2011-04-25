package com.mastertechsoftware.stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

/**
 * @author Kevin Moore
 */
public interface StreamHandler<Result> {
	public static int GET_TYPE = 0;
	public static int POST_TYPE = 1;
	public static int DELETE_TYPE = 2;
	public static int PUT_TYPE = 3;

	int getType();
	Result processInputStream(InputStream stream) throws StreamException;
	void writeOutputStream(OutputStream stream) throws StreamException;
	void setupConnection(URLConnection connection);
	void setStreamProcessor(StreamProcessor<Result> processor);
}

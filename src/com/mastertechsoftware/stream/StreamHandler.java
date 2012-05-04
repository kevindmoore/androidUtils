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

    /**
     * Return the Stream type
     * 
     * @return the stream type
     */
    StreamType getType();

    /**
     * Add any extra settings to the current connection
     * 
     * @param connection
     */
    void setupConnection(URLConnection connection);

    /**
     * Process the incoming input stream.
     * 
     * @param stream
     * @param contentLength length of content
     * @return Result object
     * @throws StreamException
     */
	Result processInputStream(InputStream stream, long contentLength) throws StreamException;

    /**
     * Write to the output stream. POST/PUT types
     * 
     * @param stream
     * @throws StreamException
     */
	void writeOutputStream(OutputStream stream) throws StreamException;

    /**
     * Set the current stream processor
     * 
     * @param processor
     */
	void setStreamProcessor(StreamProcessor<Result> processor);
}

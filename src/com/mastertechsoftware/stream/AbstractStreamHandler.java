package com.mastertechsoftware.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

/**
 * @author Kevin Moore
 */
public class AbstractStreamHandler<Result> implements StreamHandler<Result> {
	protected StreamProcessor<Result> processor;

    protected int BUFFER_SIZE = 512;

    public StreamType getType() {
        return StreamType.GET_TYPE;
	}

	public Result processInputStream(InputStream stream, long contentLength) throws StreamException {
		return null;
	}

	public void writeOutputStream(OutputStream stream) throws StreamException {

	}

	public void setupConnection(URLConnection connection) {

	}

    /**
     * Read the input stream into a string.
     * 
     * @param stream
     * @return input
     * @throws IOException
     */
    public String readInputStream(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();

        byte[] aBuffer = new byte[BUFFER_SIZE];
        int aCount = 0;
        while ((aCount = stream.read(aBuffer, 0, BUFFER_SIZE)) != -1) {
            builder.append(new String(aBuffer, 0, aCount));
        }
        return builder.toString();
    }

	public void setStreamProcessor(StreamProcessor<Result> processor) {
		this.processor = processor;
	}

	public StreamProcessor<Result> getProcessor() {
		return processor;
	}
}

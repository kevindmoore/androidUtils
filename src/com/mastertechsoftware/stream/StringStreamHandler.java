package com.mastertechsoftware.stream;

import com.mastertechsoftware.util.log.Logger;

import java.io.IOException;
import java.io.InputStream;
/**
 * Generic String Stream Handler
 */
public class StringStreamHandler extends AbstractStreamHandler<String> {
	protected StreamType streamType = StreamType.GET;

	public StringStreamHandler(StreamType streamType) {
		this.streamType = streamType;
	}

	public StringStreamHandler() {
	}

	@Override
	public StreamType getType() {
		return streamType;
	}

	@Override
	public String processInputStream(InputStream stream, long contentLength) throws StreamException {
		try {
			return readInputStream(stream);
		} catch (OutOfMemoryError e) {
			// Large Strings can cause this error
			Logger.error(this, "Out of Memory", e);
			throw getProcessor().createStreamException(null, e);
		} catch (IOException e) {
			Logger.error(this, "Problems reading input stream", e);
			throw getProcessor().createStreamException(null, e);
		}
	}
}

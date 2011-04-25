package com.mastertechsoftware.stream;

/**
 * @author Kevin Moore
 */
public class StreamException extends Exception {
	public final static int STREAM_EXCEPTION_TYPE = 0;
	public final static int IO_EXCEPTION_TYPE = 1;
	public final static int MALFORMED_URL_TYPE = 2;
	public final static int FILE_NOT_FOUND_TYPE = 3;
	public final static int SOCKET_EXCEPTION_TYPE = 4;
	public final static int UNKNOWN_HOST_EXCEPTION_TYPE = 5;
	public final static int EXCEPTION_TYPE = 6;

	protected int exceptionType;

	public StreamException() {
	}

	public StreamException(String message) {
		super(message);
	}

	public StreamException(String message, Throwable cause) {
		super(message, cause);
	}

	public StreamException(Throwable cause) {
		super(cause);
	}

	public int getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(int exceptionType) {
		this.exceptionType = exceptionType;
	}
}

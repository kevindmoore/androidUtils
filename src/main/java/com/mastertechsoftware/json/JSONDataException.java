package com.mastertechsoftware.json;

/**
 * Exception for JSONData errors
 */
public class JSONDataException extends Exception {

	public enum EXCEPTION_TYPE {
		INVALID_JSON,
		SERVER_ERROR
	}
	protected EXCEPTION_TYPE type;

	public JSONDataException() {
	}

	public JSONDataException(EXCEPTION_TYPE type) {
		this.type = type;
	}

    public JSONDataException(EXCEPTION_TYPE type, Throwable throwable) {
        super(throwable);
        this.type = type;
    }

    public JSONDataException(String detailMessage) {
		super(detailMessage);
	}

	public JSONDataException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public JSONDataException(Throwable throwable) {
		super(throwable);
	}

	public EXCEPTION_TYPE getType() {
		return type;
	}

	public void setType(EXCEPTION_TYPE type) {
		this.type = type;
	}
}

package com.mastertechsoftware.stream;

import com.mastertechsoftware.util.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * @author Kevin Moore
 */
public class StreamProcessor<Result> {
	private static int defaultBufferSize = 8192;
	public static int DEFAULT_BUFFER_LENGTH = 64000;
	protected HttpURLConnection connection;
	protected String urlString;
	protected URL url;
	protected StreamHandler<Result> streamHandler;
	protected int bufferLength = DEFAULT_BUFFER_LENGTH;
	 //the socket timeout, to prevent blocking if the server connection is lost.
	 protected final int CONNECTION_TIMEOUT = 500;
	 protected final int READ_TIMEOUT = 30 * 1000;

	/**
	 * Constructor.
	 * @param url
	 * @param streamHandler - stream handle that expects Result to be returned
	 */
	public StreamProcessor(String url, StreamHandler<Result> streamHandler) {
		this.urlString = url;
		this.streamHandler = streamHandler;
	}

	public StreamProcessor(URL url, StreamHandler<Result> streamHandler) {
		this.url = url;
		this.streamHandler = streamHandler;
	}

	/**
	 * Main routine to do the connection, process and then close streams
	 * @return Result
	 * @throws StreamException
	 */
	public Result startStream() throws StreamException {
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		streamHandler.setStreamProcessor(this);
		try {
			if (url == null) {
				url = new URL(urlString);
			}
	        connection = (HttpURLConnection)url.openConnection();
			setupConnection(connection);

			switch (streamHandler.getType()) {
				case StreamHandler.GET_TYPE:
				case StreamHandler.DELETE_TYPE:
					inputStream = new BufferedInputStream(connection.getInputStream(), defaultBufferSize);
					break;

				case StreamHandler.PUT_TYPE:
				case StreamHandler.POST_TYPE:
					outputStream = new BufferedOutputStream(connection.getOutputStream(), defaultBufferSize);
					streamHandler.writeOutputStream(outputStream);
					inputStream = new BufferedInputStream(connection.getInputStream(), defaultBufferSize);
					break;

			}

			return streamHandler.processInputStream(inputStream);
		}
		catch (StreamException e) {
			throw e;
		}
		catch (Exception e) {
			if (connection != null) {
				throw new StreamException(parseResponse(connection.getErrorStream()), e);
			}
			throw new StreamException("StreamProcessor.startStream", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					Logger.error("StreamProcessor.startStream. Could not close input stream", e);
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					Logger.error("StreamProcessor.startStream. Could not close output stream", e);
				}
			}
		}
	}

	/**
	 * Main routine to do the connection, process and then close streams
	 * @param stream
	 * @throws StreamException generic exception
	 */
	public void copyStream(OutputStream stream) throws StreamException {
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		streamHandler.setStreamProcessor(this);
		StreamException exception = null;
		try {
			if (url == null) {
				url = new URL(urlString);
			}
			connection = (HttpURLConnection)url.openConnection();
			connection.setConnectTimeout(CONNECTION_TIMEOUT);
			connection.setReadTimeout(READ_TIMEOUT);
			inputStream = new BufferedInputStream(connection.getInputStream(), bufferLength);
			outputStream = new BufferedOutputStream(stream, bufferLength);
			byte[] buffer = new byte[bufferLength];
			int read = inputStream.read(buffer);
			while (read > 0) {
				outputStream.write(buffer, 0, read);
				read = inputStream.read(buffer);
			}
			outputStream.flush();
		} catch (MalformedURLException e) {
			exception = new StreamException(e);
			exception.setExceptionType(StreamException.MALFORMED_URL_TYPE);
			throw exception;
		} catch (FileNotFoundException e) {
			exception = new StreamException(e);
			exception.setExceptionType(StreamException.FILE_NOT_FOUND_TYPE);
			throw exception;
			// Both of these next errors could be a disconnect from the internet
		} catch (SocketException e) {
			exception = new StreamException(e);
			exception.setExceptionType(StreamException.SOCKET_EXCEPTION_TYPE);
			throw exception;
		} catch (SocketTimeoutException e) {
			exception = new StreamException(e);
			exception.setExceptionType(StreamException.SOCKET_TIMEOUT_EXCEPTION_TYPE);
			throw exception;
		} catch (UnknownHostException e) {
			exception = new StreamException(e);
			exception.setExceptionType(StreamException.UNKNOWN_HOST_EXCEPTION_TYPE);
			throw exception;
		} catch (IOException e) {
			exception = new StreamException(e);
			exception.setExceptionType(StreamException.IO_EXCEPTION_TYPE);
			throw exception;
		} catch (Exception e) {
			if (connection != null) {
				InputStream errorStream = connection.getErrorStream();
				String message = null;
				if (errorStream != null) {
					message = parseResponse(errorStream);
				}
				if (message != null && message.length() > 0) {
					exception = new StreamException(message, e);
				} else {
					exception = new StreamException(e);
				}
				exception.setExceptionType(StreamException.EXCEPTION_TYPE);
				throw exception;
			}
			exception = new StreamException(e);
			exception.setExceptionType(StreamException.EXCEPTION_TYPE);
			throw exception;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					Logger.error("StreamProcessor.startStream. Could not close input stream", e);
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					Logger.error("StreamProcessor.startStream. Could not close output stream", e);
				}
			}
		}
	}

	/**
	 * Set the buffer length for copying a stream
	 * @param bufferLength
	 */
	public void setBufferLength(int bufferLength) {
		this.bufferLength = bufferLength;
	}

	/**
	 * Get the Error stream.
	 * @return InputStream
	 */
	public InputStream getErrorStream() {
		return connection.getErrorStream();
	}

	/**
	 * Get the response code
	 * @return -1 for an error, otherwise what the connection returns
	 * @throws StreamException
	 */
	public int getResponseCode() throws StreamException {
		if (connection == null) {
			return -1;
		}
		try {
			return connection.getResponseCode();
		} catch (IOException e) {
			throw new StreamException("StreamProcessor.getResponseCode.", e);
		}
	}

	/**
	 * Check for valid response code
	 * @param code
	 * @return true if valid
	 */
	public boolean isValidResponseCode(int code) {
		if (code >= 200 && code < 300) {
			return true;
		}
		return false;
	}

	/**
	 * Depending on the connection type, setup the input/output settings. Let the handler do some setup
	 * @param connection
	 * @throws StreamException
	 */
	private void setupConnection(HttpURLConnection connection) throws StreamException {
		try {
			switch (streamHandler.getType()) {
				case StreamHandler.GET_TYPE:
					connection.setRequestMethod("GET");
					connection.setDoInput(true);
					connection.setDoOutput(false);
					break;

				case StreamHandler.PUT_TYPE:
					connection.setRequestMethod("PUT");
					connection.setDoInput(true);
					connection.setDoOutput(true);
					break;

				case StreamHandler.POST_TYPE:
					connection.setRequestMethod("POST");
					connection.setDoInput(true);
					connection.setDoOutput(true);
					break;

				case StreamHandler.DELETE_TYPE:
					connection.setRequestMethod("DELETE");
					connection.setDoInput(true);
					connection.setDoOutput(false);
					break;

				default:
					throw new StreamException("StreamProcessor.setupConnection. Invalid type");
			}
			streamHandler.setupConnection(connection);
		} catch (ProtocolException e) {
			throw new StreamException("StreamProcessor.setupConnection", e);
		}
	}

	/**
	 * Read the error string from the error input stream.
	 * @param is
	 * @return error string
	 */
	public static String parseResponse(InputStream is) {
		if (is == null) {
			return "";
		}
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(is), defaultBufferSize);
			String	line;
			StringBuilder builder = new StringBuilder();
			try {
				while ((line = bufferedReader.readLine()) != null)
					builder.append(line);
			}
			catch (IOException e) {
				Logger.error("cannot log response", e);
			}
			return builder.toString();
		} catch (Exception e) {
			return "";
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					Logger.error("problems closing response", e);
				}
			}
		}
	}

}

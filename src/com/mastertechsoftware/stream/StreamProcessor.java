package com.mastertechsoftware.stream;

import com.mastertechsoftware.util.log.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

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
	public final static int TEN_SECONDS = 10 * 1000;
	protected static int defaultBufferSize = 8192;
	protected static int DEFAULT_BUFFER_LENGTH = 64000;
	protected HttpURLConnection connection;
	protected String urlString;
	protected URL url;
	protected StreamHandler<Result> streamHandler;
	protected int bufferLength = DEFAULT_BUFFER_LENGTH;
	private static final String USER_AGENT = "Android/1.0";
	//the socket timeout, to prevent blocking if the server connection is lost.
	protected final int CONNECTION_TIMEOUT = 20 * 1000;
	protected final int READ_TIMEOUT = 30 * 1000;
	protected DefaultHttpClient httpClient;
	protected long contentLength = 0;
	protected int connectionTimeout = CONNECTION_TIMEOUT;
	protected int readTimeout = READ_TIMEOUT;

	/**
	 * Constructor.
	 *
	 * @param url
	 * @param streamHandler - stream handle that expects Result to be returned
	 */
	public StreamProcessor(String url, StreamHandler<Result> streamHandler) {
		this.urlString = url;
		this.streamHandler = streamHandler;
	}

	public StreamProcessor(URL url, StreamHandler<Result> streamHandler) {
		this.url = url;
		urlString = url.toString();
		this.streamHandler = streamHandler;
	}

	public void setUrlString(String urlString) {
		this.urlString = urlString;
	}

	public void setUrl(URL url) {
		this.url = url;
		urlString = url.toString();
	}

	/**
	 * If we get a connection timeout error, try increasing the timeout
	 * @param connectionTimeout
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * If we get a read timeout error, try increasing the timeout
	 * @param readTimeout
	 */
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	/**
	 * Get the connection timeout value
	 * @return
	 */
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * Get the read timeout value
	 * @return
	 */
	public int getReadTimeout() {
		return readTimeout;
	}

	/**
	 * Main routine to do the connection, process and then close streams
	 *
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
			connection = (HttpURLConnection) url.openConnection();
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

			return streamHandler.processInputStream(inputStream, 0);
		} catch (StreamException e) {
			throw e;
		} catch (Exception e) {
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
	 * Use HttpClient to get a InputStream and process it
	 *
	 * @param url
	 * @param data
	 * @return Result
	 * @throws StreamException
	 */
	public Result processInputStream(URL url, HttpEntity data) throws StreamException {
		return processInputStream(url.toString(), data);
	}

	/**
	 * Use HttpClient to get a InputStream and process it
	 *
	 * @param url
	 * @param data
	 * @return Result
	 * @throws StreamException
	 */
	public Result processInputStream(String url, HttpEntity data) throws StreamException {
		StreamException exception = null;
		HttpParams params = new BasicHttpParams();

		// Turn off stale checking.  Our connections break all the time anyway,
		// and it's not worth it to pay the penalty of checking every time.
		HttpConnectionParams.setStaleCheckingEnabled(params, false);

		// Default connection and socket timeout of 20 seconds.  Tweak to taste.
		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);
		HttpConnectionParams.setSocketBufferSize(params, bufferLength);

		// Follow redirects as this usually can happen several times
		HttpClientParams.setRedirecting(params, true);

		httpClient = new DefaultHttpClient(params);
		HttpResponse response = null;
		try {
			switch (streamHandler.getType()) {
				case StreamHandler.GET_TYPE:
					response = httpClient.execute(new HttpGet(url));
					break;

				case StreamHandler.PUT_TYPE:
					HttpPut httpPut = new HttpPut(url);
					httpPut.setEntity(data);
					response = httpClient.execute(httpPut);
					break;

				case StreamHandler.POST_TYPE:
					HttpPost httpPost = new HttpPost(url);
					httpPost.setEntity(data);
					response = httpClient.execute(httpPost);
					break;

				case StreamHandler.DELETE_TYPE:
					response = httpClient.execute(new HttpDelete(url));
					break;

				default:
					throw new StreamException("StreamProcessor.processInputStream. Invalid type");
			}
			if (response != null) {
				if (response.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						contentLength = entity.getContentLength();
						InputStream content = entity.getContent();
						if (content != null) {
							return streamHandler.processInputStream(new BufferedInputStream(content, defaultBufferSize), contentLength);
						}
					}
				} else {
					Logger.error("StreamProcessor.processInputStream. Response: " + response.getStatusLine().getStatusCode());
					exception = new StreamException("StreamProcessor.processInputStream. Response: " + response.getStatusLine().getStatusCode());
					exception.setExceptionType(StreamException.EXCEPTION_TYPE);
					throw exception;
				}
			}
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
			exception = new StreamException(e);
			exception.setExceptionType(StreamException.EXCEPTION_TYPE);
			throw exception;
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
		return null;
	}

	public long getContentLength() {
		if (connection != null) {
			return connection.getContentLength();
		} else if (httpClient != null) {
			return contentLength;
		}
		return 0;
	}

	/**
	 * Main routine to do the connection, process and then close streams
	 *
	 * @param stream
	 * @throws StreamException generic exception
	 */
	public void copyStream(OutputStream stream) throws StreamException {
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		streamHandler.setStreamProcessor(this);
		StreamException exception = null;
		try {
			HttpParams params = new BasicHttpParams();

			// Turn off stale checking.  Our connections break all the time anyway,
			// and it's not worth it to pay the penalty of checking every time.
			HttpConnectionParams.setStaleCheckingEnabled(params, false);

			// Default connection and socket timeout of 20 seconds.  Tweak to taste.
			HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
			HttpConnectionParams.setSoTimeout(params, readTimeout);
			HttpConnectionParams.setSocketBufferSize(params, bufferLength);

			// Follow redirects as this usually can happen several times
			HttpClientParams.setRedirecting(params, true);

			httpClient = new DefaultHttpClient(params);
			HttpResponse response = null;

			response = httpClient.execute(new HttpGet(urlString));
			if (response != null) {
				if (response.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						contentLength = entity.getContentLength();
						InputStream content = entity.getContent();
						if (content != null) {
							inputStream = new BufferedInputStream(content, bufferLength);
							outputStream = new BufferedOutputStream(stream, bufferLength);
							byte[] buffer = new byte[bufferLength];
							int read = inputStream.read(buffer);
							while (read > 0) {
								outputStream.write(buffer, 0, read);
								read = inputStream.read(buffer);
							}
							outputStream.flush();
						}
					}
				} else {
					Logger.error("StreamProcessor.processInputStream. Response: " + response.getStatusLine().getStatusCode());
					exception = new StreamException("StreamProcessor.processInputStream. Response: " + response.getStatusLine().getStatusCode());
					exception.setExceptionType(StreamException.EXCEPTION_TYPE);
					throw exception;
				}
			}

		} catch (MalformedURLException e) {
			exception = new StreamException(e.getMessage(), e);
			exception.setExceptionType(StreamException.MALFORMED_URL_TYPE);
			throw exception;
		} catch (FileNotFoundException e) {
			exception = new StreamException(e.getMessage(), e);
			exception.setExceptionType(StreamException.FILE_NOT_FOUND_TYPE);
			throw exception;
			// Both of these next errors could be a disconnect from the internet
		} catch (SocketException e) {
			exception = new StreamException(e.getMessage(), e);
			exception.setExceptionType(StreamException.SOCKET_EXCEPTION_TYPE);
			throw exception;
		} catch (SocketTimeoutException e) {
			exception = new StreamException(e.getMessage(), e);
			exception.setExceptionType(StreamException.SOCKET_TIMEOUT_EXCEPTION_TYPE);
			throw exception;
		} catch (UnknownHostException e) {
			exception = new StreamException(e.getMessage(), e);
			exception.setExceptionType(StreamException.UNKNOWN_HOST_EXCEPTION_TYPE);
			throw exception;
		} catch (ClientProtocolException e) {
			exception = new StreamException(e.getMessage(), e);
			exception.setExceptionType(StreamException.CLIENT_PROTOCOL_EXCEPTION_TYPE);
			throw exception;
		} catch (IOException e) {
			exception = new StreamException(e.getMessage(), e);
			exception.setExceptionType(StreamException.IO_EXCEPTION_TYPE);
			throw exception;
		} catch (Exception e) {
			exception = new StreamException(e.getMessage(), e);
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
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
	}

	/**
	 * Set the buffer length for copying a stream
	 *
	 * @param bufferLength
	 */
	public void setBufferLength(int bufferLength) {
		this.bufferLength = bufferLength;
	}

	/**
	 * Get the Error stream.
	 *
	 * @return InputStream
	 */
	public InputStream getErrorStream() {
		return connection.getErrorStream();
	}

	/**
	 * Get the response code
	 *
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
	 *
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
	 *
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
	 *
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
			String line;
			StringBuilder builder = new StringBuilder();
			try {
				while ((line = bufferedReader.readLine()) != null)
					builder.append(line);
			} catch (IOException e) {
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

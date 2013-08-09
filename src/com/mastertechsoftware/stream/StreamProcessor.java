package com.mastertechsoftware.stream;

import com.mastertechsoftware.util.log.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;

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
import java.util.HashMap;
import java.util.Map;

/**
 * Class for handling Http streams
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
	protected Map<String, String> requestHeaders = new HashMap<String, String>();
	protected Map<String, String> requestParams = new HashMap<String, String>();
	protected CookieStore cookieStore;
	protected boolean useParams = true;
	protected BasicHttpContext localContext;
	protected String userName, password;

	/**
	 * Constructor.
	 *
	 * @param streamHandler - stream handle that expects Result to be returned
	 */
	public StreamProcessor(String url, StreamHandler<Result> streamHandler) {
		this.urlString = url;
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			Logger.error(this, "Problems with url " + url, e);
		}
		this.streamHandler = streamHandler;
	}

	/**
	 * Constructor.
	 */
	public StreamProcessor(URL url, StreamHandler<Result> streamHandler) {
		this.url = url;
		urlString = url.toString();
		this.streamHandler = streamHandler;
	}

	/**
	 * Set the current URL
	 */
	public void setUrlString(String urlString) {
		this.urlString = urlString;
	}

	/**
	 * Set the current URL
	 */
	public void setUrl(URL url) {
		this.url = url;
		urlString = url.toString();
	}

	/**
	 * Set whether we should use our own parameters for http call
	 */
	public void setUseParams(boolean useParams) {
		this.useParams = useParams;
	}

	/**
	 * Set a map of request headers. Will be <name,value> pairs
	 */
	public void setRequestHeaders(Map<String, String> headers) {
		this.requestHeaders = headers;
	}

	/**
	 * Add a new request header
	 */
	public void addRequestHeader(String name, String value) {
		this.requestHeaders.put(name, value);
	}

	/**
	 * Add a request Parameter
	 */
	public void addRequestParam(String name, String value) {
		this.requestParams.put(name, value);
	}

	/**
	 * If we get a connection timeout error, try increasing the timeout
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * If we get a read timeout error, try increasing the timeout
	 */
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	/**
	 * Get the connection timeout value
	 *
	 * @return connection timeout
	 */
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * Get the read timeout value
	 *
	 * @return read timeout
	 */
	public int getReadTimeout() {
		return readTimeout;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Default process method. Take the given URL and start processing
	 *
	 * @return Result
	 */
	public Result processInputStream() throws StreamException {
		return processInputStream(url.toString(), null);
	}

	/**
	 * Process the input stream with the given HttpEntity
	 *
	 * @return Result
	 */
	public Result processInputStream(HttpEntity data) throws StreamException {
		return processInputStream(url.toString(), data);
	}

	/**
	 * Use HttpClient to get a InputStream and process it
	 *
	 * @return Result
	 */
	public Result processInputStream(URL url, HttpEntity data) throws StreamException {
		return processInputStream(url.toString(), data);
	}

	/**
	 * Use HttpClient to get a InputStream and process it
	 *
	 * @return Result
	 */
	public Result processInputStream(String url, HttpEntity data) throws StreamException {
		StreamException exception = null;

		if (useParams) {
			//            HttpParams params = createHttpParams();

			httpClient = new DefaultHttpClient();
			HttpParams httpParams = httpClient.getParams();
			copyHttpParams(httpParams);
			httpClient = new DefaultHttpClient(httpParams);
		} else {
			httpClient = new DefaultHttpClient();
		}
		setLoginInfo(url, httpClient);
		setClientCookieStore();
		HttpResponse response = null;
		try {
			switch (streamHandler.getType()) {
				case GET:
					HttpGet request = new HttpGet(url);
					addRequestHeaders(request);
					response = httpClient.execute(request);
					break;

				case PUT:
					HttpPut httpPut = new HttpPut(url);
					httpPut.setEntity(data);
					addRequestHeaders(httpPut);
					response = httpClient.execute(httpPut);
					break;

				case POST:
					HttpPost httpPost = new HttpPost(url);
					httpPost.setEntity(data);
					addRequestHeaders(httpPost);
					response = httpClient.execute(httpPost);
					break;

				case DELETE:
					HttpDelete httpDelete = new HttpDelete(url);
					addRequestHeaders(httpDelete);
					response = httpClient.execute(httpDelete);
					break;

				default:
					throw new StreamException("StreamProcessor.processInputStream. Invalid type");
			}
			if (response != null) {
				if (isValidResponseCode(response.getStatusLine().getStatusCode())) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						contentLength = entity.getContentLength();
						InputStream content = entity.getContent();
						if (content != null) {
							return streamHandler.processInputStream(new BufferedInputStream(content, defaultBufferSize), contentLength);
						}
					}
				} else {
					Logger.error(this,
						"StreamProcessor.processInputStream. Response: " + response.getStatusLine().getStatusCode() + " for url " + url);
					exception = new StreamException(
						"StreamProcessor.processInputStream. Response: " + response.getStatusLine().getStatusCode() + " for url " + url);
					exception.setResponseCode(getResponseCode(response));
					exception.setResponseMessage(getResponseMessage(response));
					exception.setExceptionType(StreamException.STREAM_EXCEPTION_TYPE);
					throw exception;
				}
			}
		} catch (MalformedURLException e) {
			Logger.error(this, "StreamProcessor.processInputStream. Problems Processing " + url);
			exception = createMalformedURLException(response, e);
			throw exception;
		} catch (FileNotFoundException e) {
			Logger.error(this, "StreamProcessor.processInputStream. Problems Processing " + url);
			exception = createFileNotFoundException(response, e);
			throw exception;
			// Both of these next errors could be a disconnect from the internet
		} catch (SocketException e) {
			Logger.error(this, "StreamProcessor.processInputStream. Problems Processing " + url);
			exception = createStreamException(response, e);
			throw exception;
		} catch (SocketTimeoutException e) {
			Logger.error(this, "StreamProcessor.processInputStream. Problems Processing " + url);
			exception = createSocketTimeoutException(response, e);
			throw exception;
		} catch (UnknownHostException e) {
			Logger.error(this, "StreamProcessor.processInputStream. Problems Processing " + url);
			exception = createUnknownHostException(response, e);
			throw exception;
		} catch (IOException e) {
			Logger.error(this, "StreamProcessor.processInputStream. Problems Processing " + url);
			exception = createIOException(response, e);
			throw exception;
		} catch (StreamException e) {
			throw e;
		} catch (Exception e) {
			Logger.error(this, "StreamProcessor.processInputStream. Problems Processing " + url);
			exception = createStreamException(response, e);
			throw exception;
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
		return null;
	}

	private void setLoginInfo(String urlString, DefaultHttpClient httpclient) {
		if (userName != null && password != null) {
			try {
				URL url = new URL(urlString);
				HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());

				httpclient.getCredentialsProvider().setCredentials(
					new AuthScope(targetHost.getHostName(), targetHost.getPort()),
					new UsernamePasswordCredentials(userName, password));

	//			// Create AuthCache instance
	//			// Generate BASIC scheme object and add it to the local auth cache
	//			BasicScheme basicAuth = new BasicScheme();
	//
	//			// Add AuthCache to the execution context
	//			localContext = new BasicHttpContext();
			} catch (MalformedURLException e) {
				Logger.error(this, "Problems settings username/password", e);
			}
		}
	}

	public StreamException createStreamException(HttpResponse response, Exception e) {
		StreamException exception;
		exception = new StreamException(e);
		exception.setExceptionType(StreamException.EXCEPTION_TYPE);
		exception.setResponseCode(getResponseCode(response));
		exception.setResponseMessage(getResponseMessage(response));
		if (exception.getResponseMessage() == null) {
			exception.setResponseMessage(e.getMessage());
		}
		return exception;
	}

	public StreamException createStreamException(HttpResponse response, OutOfMemoryError e) {
		StreamException exception;
		exception = new StreamException(e);
		exception.setExceptionType(StreamException.EXCEPTION_TYPE);
		exception.setResponseCode(getResponseCode(response));
		exception.setResponseMessage(getResponseMessage(response));
		if (exception.getResponseMessage() == null) {
			exception.setResponseMessage(e.getMessage());
		}
		return exception;
	}

	public StreamException createMalformedURLException(HttpResponse response, MalformedURLException e) {
		StreamException exception;
		exception = new StreamException(e);
		exception.setExceptionType(StreamException.MALFORMED_URL_TYPE);
		exception.setResponseCode(getResponseCode(response));
		exception.setResponseMessage(getResponseMessage(response));
		if (exception.getResponseMessage() == null) {
			exception.setResponseMessage(e.getMessage());
		}
		return exception;
	}

	public StreamException createFileNotFoundException(HttpResponse response, FileNotFoundException e) {
		StreamException exception;
		exception = new StreamException(e);
		exception.setExceptionType(StreamException.FILE_NOT_FOUND_TYPE);
		exception.setResponseCode(getResponseCode(response));
		exception.setResponseMessage(getResponseMessage(response));
		if (exception.getResponseMessage() == null) {
			exception.setResponseMessage(e.getMessage());
		}
		return exception;
	}

	public StreamException createStreamException(HttpResponse response, SocketException e) {
		StreamException exception;
		exception = new StreamException(e);
		exception.setExceptionType(StreamException.SOCKET_EXCEPTION_TYPE);
		exception.setResponseCode(getResponseCode(response));
		exception.setResponseMessage(getResponseMessage(response));
		if (exception.getResponseMessage() == null) {
			exception.setResponseMessage(e.getMessage());
		}
		return exception;
	}

	public StreamException createSocketTimeoutException(HttpResponse response, SocketTimeoutException e) {
		StreamException exception;
		exception = new StreamException(e);
		exception.setExceptionType(StreamException.SOCKET_TIMEOUT_EXCEPTION_TYPE);
		exception.setResponseCode(getResponseCode(response));
		exception.setResponseMessage(getResponseMessage(response));
		if (exception.getResponseMessage() == null) {
			exception.setResponseMessage(e.getMessage());
		}
		return exception;
	}

	public StreamException createUnknownHostException(HttpResponse response, UnknownHostException e) {
		StreamException exception;
		exception = new StreamException(e);
		exception.setExceptionType(StreamException.UNKNOWN_HOST_EXCEPTION_TYPE);
		exception.setResponseCode(getResponseCode(response));
		exception.setResponseMessage(getResponseMessage(response));
		if (exception.getResponseMessage() == null) {
			exception.setResponseMessage(e.getMessage());
		}
		return exception;
	}

	public StreamException createIOException(HttpResponse response, IOException e) {
		StreamException exception;
		exception = new StreamException(e);
		exception.setExceptionType(StreamException.IO_EXCEPTION_TYPE);
		exception.setResponseCode(getResponseCode(response));
		exception.setResponseMessage(getResponseMessage(response));
		if (exception.getResponseMessage() == null) {
			exception.setResponseMessage(e.getMessage());
		}
		return exception;
	}

	public StreamException createClientProtocolException(HttpResponse response, ClientProtocolException e) {
		StreamException exception;
		exception = new StreamException(e.getMessage(), e);
		exception.setExceptionType(StreamException.CLIENT_PROTOCOL_EXCEPTION_TYPE);
		exception.setResponseCode(getResponseCode(response));
		exception.setResponseMessage(getResponseMessage(response));
		return exception;
	}

	/**
	 * Create common HTTP Params with our settings
	 *
	 * @return HttpParams
	 */
	private HttpParams createHttpParams() {
		HttpParams params = new BasicHttpParams();

		// Turn off stale checking. Our connections break all the time anyway,
		// and it's not worth it to pay the penalty of checking every time.
		HttpConnectionParams.setStaleCheckingEnabled(params, false);

		// Default connection and socket timeout of 20 seconds. Tweak to taste.
		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);
		// This seems to cause a problem with puts
		//        HttpConnectionParams.setSocketBufferSize(params, bufferLength);

		// Follow redirects as this usually can happen several times
		HttpClientParams.setRedirecting(params, true);

		// Add any raw parameters like User-Agent
		if (requestParams.size() > 0) {
			for (String key : requestParams.keySet()) {
				params.setParameter(key, requestParams.get(key));
			}
		}
		return params;
	}

	/**
	 * Copy parameters to existing
	 */
	private void copyHttpParams(HttpParams params) {

		// Turn off stale checking. Our connections break all the time anyway,
		// and it's not worth it to pay the penalty of checking every time.
		HttpConnectionParams.setStaleCheckingEnabled(params, false);

		// Default connection and socket timeout of 20 seconds. Tweak to taste.
		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);
		// This seems to cause a problem with puts
		//        HttpConnectionParams.setSocketBufferSize(params, bufferLength);

		// Follow redirects as this usually can happen several times
		HttpClientParams.setRedirecting(params, true);

		// Add any raw parameters like User-Agent
		if (requestParams.size() > 0) {
			for (String key : requestParams.keySet()) {
				params.setParameter(key, requestParams.get(key));
			}
		}
	}

	/**
	 * Return the current Cookie store. Note that this will not be available until the request has been completed.
	 *
	 * @return CookieStore
	 */
	public CookieStore getCookieStore() {
		if (httpClient == null) {
			return null;
		}
		return httpClient.getCookieStore();
	}

	/**
	 * Set the cookie store to be set before the input stream is processed
	 */
	public void setCookieStore(CookieStore cookieStore) {
		this.cookieStore = cookieStore;
	}

	/**
	 * If the user has set a cookie store, set the client's store.
	 */
	protected void setClientCookieStore() {
		if (cookieStore != null && httpClient != null) {
			httpClient.setCookieStore(cookieStore);
		}
	}

	/**
	 * Copy from inputstream to outputstream
	 *
	 * @throws StreamException generic exception
	 */
	public void copyStream(OutputStream stream) throws StreamException {
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		streamHandler.setStreamProcessor(this);
		StreamException exception = null;
		HttpResponse response = null;
		try {
			HttpParams params = createHttpParams();

			httpClient = new DefaultHttpClient(params);
			setClientCookieStore();

			HttpGet request = new HttpGet(urlString);
			addRequestHeaders(request);
			response = httpClient.execute(request);
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
					Logger.error(this, "StreamProcessor.processInputStream. Response: " + response.getStatusLine().getStatusCode());
					exception = new StreamException(
						"StreamProcessor.processInputStream. Response: " + response.getStatusLine().getStatusCode());
					exception.setResponseCode(getResponseCode(response));
					exception.setResponseMessage(getResponseMessage(response));
					exception.setExceptionType(StreamException.STREAM_EXCEPTION_TYPE);
					throw exception;
				}
			}

		} catch (MalformedURLException e) {
			Logger.error(this, "StreamProcessor.processInputStream. Problems Processing " + urlString);
			exception = createMalformedURLException(response, e);
			throw exception;
		} catch (FileNotFoundException e) {
			Logger.error(this, "StreamProcessor.processInputStream. Problems Processing " + urlString);
			exception = createFileNotFoundException(response, e);
			throw exception;
			// Both of these next errors could be a disconnect from the internet
		} catch (SocketException e) {
			Logger.error(this, "StreamProcessor.processInputStream. Problems Processing " + urlString);
			exception = createStreamException(response, e);
			throw exception;
		} catch (SocketTimeoutException e) {
			Logger.error(this, "StreamProcessor.processInputStream. Problems Processing " + urlString);
			exception = createSocketTimeoutException(response, e);
			throw exception;
		} catch (UnknownHostException e) {
			Logger.error(this, "StreamProcessor.processInputStream. Problems Processing " + urlString);
			exception = createUnknownHostException(response, e);
			throw exception;
		} catch (ClientProtocolException e) {
			exception = createClientProtocolException(response, e);
			throw exception;
		} catch (IOException e) {
			Logger.error(this, "StreamProcessor.processInputStream. Problems Processing " + urlString);
			exception = createIOException(response, e);
			throw exception;
		} catch (StreamException e) {
			throw e;
		} catch (Exception e) {
			Logger.error(this, "StreamProcessor.processInputStream. Problems Processing " + urlString);
			exception = createStreamException(response, e);
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
	 * Add any request Headers
	 */
	protected void addRequestHeaders(AbstractHttpMessage message) {
		for (String name : requestHeaders.keySet()) {
			message.addHeader(name, requestHeaders.get(name));
		}
	}

	public void debugMsg(AbstractHttpMessage message) {
		ProtocolVersion protocolVersion = message.getProtocolVersion();
		if (message instanceof HttpRequestBase) {
			Logger.debug("Message: " + ((HttpRequestBase) message).getURI());
		}
		Logger.debug("Protocol: " + protocolVersion.getProtocol() + " ");
		for (String name : requestHeaders.keySet()) {
			Logger.debug("Header: Name: " + name + " value: " + requestHeaders.get(name));
		}
		HttpParams param = message.getParams();
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
	 * Set the buffer length for copying a stream
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
	 * Get the response Message
	 *
	 * @return String
	 */
	public String getResponseMessage() throws StreamException {
		if (connection == null) {
			return null;
		}
		try {
			return connection.getResponseMessage();
		} catch (IOException e) {
			throw new StreamException("StreamProcessor.getResponseMessage.", e);
		}
	}

	/**
	 * Get the response Message
	 *
	 * @return String
	 */
	public static String getResponseMessage(HttpResponse response) {
		if (response == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			InputStream content = null;
			try {
				content = entity.getContent();
				builder.append(parseResponse(content));
				return builder.toString();
			} catch (IOException e) {
				Logger.error(StreamProcessor.class.getSimpleName(), "Problems parsing Response entity");
			} catch (Exception e) {
				// Can sometimes get IllegalStateException when content has been consumed already
				Logger.error(StreamProcessor.class.getSimpleName(), "Problems parsing Response entity");
			}
		}
		StatusLine statusLine = response.getStatusLine();
		if (statusLine != null) {
			builder.append(statusLine.getReasonPhrase());
		}
		return builder.toString();
	}

	/**
	 * Safely return a response code
	 *
	 * @return response code
	 */
	public int getResponseCode(HttpResponse response) {
		if (response == null) {
			return -1;
		}
		StatusLine statusLine = response.getStatusLine();
		if (statusLine != null) {
			return statusLine.getStatusCode();
		}
		return -1;
	}

	/**
	 * Check for valid response code
	 *
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
	 */
	private void setupConnection(HttpURLConnection connection) throws StreamException {
		try {
			switch (streamHandler.getType()) {
				case GET:
					connection.setRequestMethod("GET");
					connection.setDoInput(true);
					connection.setDoOutput(false);
					break;

				case PUT:
					connection.setRequestMethod("PUT");
					connection.setDoInput(true);
					connection.setDoOutput(true);
					break;

				case POST:
					connection.setRequestMethod("POST");
					connection.setDoInput(true);
					connection.setDoOutput(true);
					break;

				case DELETE:
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
				while ((line = bufferedReader.readLine()) != null) {
					builder.append(line);
				}
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

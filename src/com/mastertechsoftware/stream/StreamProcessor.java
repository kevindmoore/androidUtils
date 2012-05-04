package com.mastertechsoftware.stream;

import com.mastertechsoftware.util.log.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
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
    protected CookieStore cookieStore;

	/**
	 * Constructor.
	 *
	 * @param url
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
     * Set a map of request headers. Will be <name,value> pairs
     * 
     * @param headers
     */
    public void setRequestHeaders(Map<String, String> headers) {
        this.requestHeaders = headers;
    }

    /**
     * Add a new request header
     * 
     * @param name
     * @param value
     */
    public void addRequestHeader(String name, String value) {
        this.requestHeaders.put(name, value);
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

	/**
     * Default process method. Take the given URL and start processing
	 *
	 * @return Result
	 * @throws StreamException
	 */
    public Result processInputStream() throws StreamException {
        return processInputStream(url.toString(), null);
			}

    /**
     * Process the input stream with the given HttpEntity
     * 
     * @param data
     * @return Result
     * @throws StreamException
     */
    public Result processInputStream(HttpEntity data) throws StreamException {
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

        HttpParams params = createHttpParams();

		httpClient = new DefaultHttpClient(params);
        setClientCookieStore();
		HttpResponse response = null;
		try {
			switch (streamHandler.getType()) {
                case GET_TYPE:
                    HttpGet request = new HttpGet(url);
                    addRequestHeaders(request);
                    response = httpClient.execute(request);
					break;

                case PUT_TYPE:
					HttpPut httpPut = new HttpPut(url);
					httpPut.setEntity(data);
                    addRequestHeaders(httpPut);
					response = httpClient.execute(httpPut);
					break;

                case POST_TYPE:
					HttpPost httpPost = new HttpPost(url);
					httpPost.setEntity(data);
                    addRequestHeaders(httpPost);
					response = httpClient.execute(httpPost);
					break;

                case DELETE_TYPE:
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
                    Logger.error(this, "StreamProcessor.processInputStream. Response: "
                            + response.getStatusLine().getStatusCode());
                    exception = new StreamException(
                            "StreamProcessor.processInputStream. Response: "
                                    + response.getStatusLine().getStatusCode());
                    exception.setResponseCode(getResponseCode(response));
                    exception.setResponseMessage(getResponseMessage(response));
                    exception.setExceptionType(StreamException.STREAM_EXCEPTION_TYPE);
					throw exception;
				}
			}
		} catch (MalformedURLException e) {
			exception = new StreamException(e);
			exception.setExceptionType(StreamException.MALFORMED_URL_TYPE);
            exception.setResponseCode(getResponseCode(response));
            exception.setResponseMessage(getResponseMessage(response));
			throw exception;
		} catch (FileNotFoundException e) {
			exception = new StreamException(e);
			exception.setExceptionType(StreamException.FILE_NOT_FOUND_TYPE);
            exception.setResponseCode(getResponseCode(response));
            exception.setResponseMessage(getResponseMessage(response));
			throw exception;
			// Both of these next errors could be a disconnect from the internet
		} catch (SocketException e) {
			exception = new StreamException(e);
			exception.setExceptionType(StreamException.SOCKET_EXCEPTION_TYPE);
            exception.setResponseCode(getResponseCode(response));
            exception.setResponseMessage(getResponseMessage(response));
			throw exception;
		} catch (SocketTimeoutException e) {
			exception = new StreamException(e);
			exception.setExceptionType(StreamException.SOCKET_TIMEOUT_EXCEPTION_TYPE);
            exception.setResponseCode(getResponseCode(response));
            exception.setResponseMessage(getResponseMessage(response));
			throw exception;
		} catch (UnknownHostException e) {
			exception = new StreamException(e);
			exception.setExceptionType(StreamException.UNKNOWN_HOST_EXCEPTION_TYPE);
            exception.setResponseCode(getResponseCode(response));
            exception.setResponseMessage(getResponseMessage(response));
			throw exception;
		} catch (IOException e) {
			exception = new StreamException(e);
			exception.setExceptionType(StreamException.IO_EXCEPTION_TYPE);
            exception.setResponseCode(getResponseCode(response));
            exception.setResponseMessage(getResponseMessage(response));
			throw exception;
        } catch (StreamException e) {
            throw e;
		} catch (Exception e) {
			exception = new StreamException(e);
			exception.setExceptionType(StreamException.EXCEPTION_TYPE);
            exception.setResponseCode(getResponseCode(response));
            exception.setResponseMessage(getResponseMessage(response));
			throw exception;
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
		return null;
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
        HttpConnectionParams.setSocketBufferSize(params, bufferLength);

        // Follow redirects as this usually can happen several times
        HttpClientParams.setRedirecting(params, true);
        return params;
    }

    /**
     * Return the current Cookie store. Note that this will not be available
     * until the request has been completed.
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
     * 
     * @param cookieStore
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
	 * @param stream
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
                    Logger.error(this, "StreamProcessor.processInputStream. Response: "
                            + response.getStatusLine().getStatusCode());
                    exception = new StreamException(
                            "StreamProcessor.processInputStream. Response: "
                                    + response.getStatusLine().getStatusCode());
                    exception.setResponseCode(getResponseCode(response));
                    exception.setResponseMessage(getResponseMessage(response));
                    exception.setExceptionType(StreamException.STREAM_EXCEPTION_TYPE);
					throw exception;
				}
			}

		} catch (MalformedURLException e) {
			exception = new StreamException(e.getMessage(), e);
			exception.setExceptionType(StreamException.MALFORMED_URL_TYPE);
            exception.setResponseCode(getResponseCode(response));
            exception.setResponseMessage(getResponseMessage(response));
			throw exception;
		} catch (FileNotFoundException e) {
			exception = new StreamException(e.getMessage(), e);
			exception.setExceptionType(StreamException.FILE_NOT_FOUND_TYPE);
            exception.setResponseCode(getResponseCode(response));
            exception.setResponseMessage(getResponseMessage(response));
			throw exception;
			// Both of these next errors could be a disconnect from the internet
		} catch (SocketException e) {
			exception = new StreamException(e.getMessage(), e);
			exception.setExceptionType(StreamException.SOCKET_EXCEPTION_TYPE);
            exception.setResponseCode(getResponseCode(response));
            exception.setResponseMessage(getResponseMessage(response));
			throw exception;
		} catch (SocketTimeoutException e) {
			exception = new StreamException(e.getMessage(), e);
			exception.setExceptionType(StreamException.SOCKET_TIMEOUT_EXCEPTION_TYPE);
            exception.setResponseCode(getResponseCode(response));
            exception.setResponseMessage(getResponseMessage(response));
			throw exception;
		} catch (UnknownHostException e) {
			exception = new StreamException(e.getMessage(), e);
			exception.setExceptionType(StreamException.UNKNOWN_HOST_EXCEPTION_TYPE);
            exception.setResponseCode(getResponseCode(response));
            exception.setResponseMessage(getResponseMessage(response));
			throw exception;
		} catch (ClientProtocolException e) {
			exception = new StreamException(e.getMessage(), e);
			exception.setExceptionType(StreamException.CLIENT_PROTOCOL_EXCEPTION_TYPE);
            exception.setResponseCode(getResponseCode(response));
            exception.setResponseMessage(getResponseMessage(response));
			throw exception;
		} catch (IOException e) {
			exception = new StreamException(e.getMessage(), e);
			exception.setExceptionType(StreamException.IO_EXCEPTION_TYPE);
            exception.setResponseCode(getResponseCode(response));
            exception.setResponseMessage(getResponseMessage(response));
			throw exception;
        } catch (StreamException e) {
            throw e;
		} catch (Exception e) {
			exception = new StreamException(e.getMessage(), e);
			exception.setExceptionType(StreamException.EXCEPTION_TYPE);
            exception.setResponseCode(getResponseCode(response));
            exception.setResponseMessage(getResponseMessage(response));
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
     * 
     * @param message
     */
    protected void addRequestHeaders(AbstractHttpMessage message) {
        for (String name : requestHeaders.keySet()) {
            message.addHeader(name, requestHeaders.get(name));
        }
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
     * Get the response Message
     * @return String
     * @throws StreamException
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
     * @param response
     * @return String
     * @throws StreamException
     */
    public String getResponseMessage(HttpResponse response) throws StreamException {
        if (response == null) {
            return null;
        }
        StatusLine statusLine = response.getStatusLine();
        if (statusLine != null) {
            return statusLine.getReasonPhrase();
        }
        return null;
    }

    /**
    * Safely return a response code
    *
    * @param response
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
                case GET_TYPE:
					connection.setRequestMethod("GET");
					connection.setDoInput(true);
					connection.setDoOutput(false);
					break;

                case PUT_TYPE:
					connection.setRequestMethod("PUT");
					connection.setDoInput(true);
					connection.setDoOutput(true);
					break;

                case POST_TYPE:
					connection.setRequestMethod("POST");
					connection.setDoInput(true);
					connection.setDoOutput(true);
					break;

                case DELETE_TYPE:
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

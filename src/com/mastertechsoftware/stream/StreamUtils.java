
package com.mastertechsoftware.stream;

import java.util.List;

/**
 * Utility class for Streams.
 */
public class StreamUtils {

    /**
     * Build a URL string with the given server, path & parameters.
     * 
     * @param server
     * @param path
     * @param params
     * @return url string
     */
    public static String buildURLString(String server, String path, List<String> params) {
        StringBuilder builder = new StringBuilder();
        builder.append(server);
        if (!server.endsWith("/")) {
            builder.append("/");
        }
        builder.append(path);
        if (!path.endsWith("/")) {
            builder.append("/");
        }
        if (params != null) {
            for (String param : params) {
                builder.append(param);
                builder.append("/");
            }
        }
        if (builder.charAt(builder.length() - 1) == '/') {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }

    public static String buildQueryString(String server, String path, List<String> params) {
        StringBuilder builder = new StringBuilder();
        builder.append(server);
        if (params != null) {
            for (String param : params) {
				int substitueIndex = path.indexOf("$");
				if (substitueIndex != -1) {
					builder.append(path.substring(0, substitueIndex));
					builder.append(param);
					path = path.substring(substitueIndex+1);
				}
            }
        }
		if (path.length() > 0) {
			builder.append(path);
		}
        if (builder.charAt(builder.length() - 1) == '/') {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }
}

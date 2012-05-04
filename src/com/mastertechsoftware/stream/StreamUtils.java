
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
}

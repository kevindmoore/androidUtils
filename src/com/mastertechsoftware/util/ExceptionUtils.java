package com.mastertechsoftware.util;

/**
 * User: kevin.moore
 */
public class ExceptionUtils {

    /**
     * Utility to go down the exception stack to find a message.
     * @param exception
     * @return exception Msg
     */
    public static String getExceptionMessage(Throwable exception) {
        String msg = exception.getMessage();
        if (msg != null) {
            return msg;
        }
        while (msg == null) {
            exception = exception.getCause();
            if (exception == null) {
                return null;
            }
            msg = exception.getMessage();
        }
        return msg;
    }
}

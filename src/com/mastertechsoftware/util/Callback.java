package com.mastertechsoftware.util;

/**
 * User: Kevin
 * Date: Dec 27, 2009
 */
public interface Callback {
    void asyncFinished(Object data);
    void processData(ParamHolder data);
}

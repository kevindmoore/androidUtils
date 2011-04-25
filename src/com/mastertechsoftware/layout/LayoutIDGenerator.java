package com.mastertechsoftware.layout;

import java.util.HashMap;

/**
 * Date: Jul 18, 2010
 */
public class LayoutIDGenerator {
    private static int STARTING_NUMBER = 6000;
    private static int currentID = STARTING_NUMBER;
    private static HashMap<String, Integer> ids = new HashMap<String, Integer>();

    public static int getNewID(String key) {
        Integer value = ids.get(key);
        if (value != null) {
            return value;
        }
        ids.put(key, currentID);
        return currentID++;
    }

    public static int getID(String key) {
        if (ids.containsKey(key)) {
            return ids.get(key);
        } else {
            return -1;
        }
    }
}

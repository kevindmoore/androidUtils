package com.mastertechsoftware.list;

import java.util.ArrayList;

/**
 * User: Kevin
 * Date: Mar 28, 2010
 * This is a list that holds map data.
 */
public class ListMap<K,V> extends ArrayList<KeyValue<K,V>> {

    public boolean add(K key, V value) {
        KeyValue<K,V> keyValue = new KeyValue<K,V>(key, value);
        return super.add(keyValue);
    }

    public void add(int index, K key, V value) {
        KeyValue<K,V> keyValue = new KeyValue<K,V>(key, value);
        super.add(index, keyValue);
    }

    public V getValue(int index) {
        return super.get(index).getValue();
    }

}

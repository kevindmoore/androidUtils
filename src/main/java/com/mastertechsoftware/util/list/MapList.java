package com.mastertechsoftware.util.list;

import java.util.*;

/**
 * Date: Aug 7, 2010
 * This is a map that allows duplicate values, like a list
 * and allows you to have a list of items for a key
 */
public class MapList<K,V> implements Map<K,V> {

    protected HashMap<K, ArrayList<V>> backingMap = new HashMap<K, ArrayList<V>>();

    public V put(K key, V value) {
        ArrayList<V> currentValue = backingMap.get(key);
        if (currentValue == null) {
            currentValue = new ArrayList<V>();
            backingMap.put(key, currentValue);
        }
        currentValue.add(value);
        return value;
    }



    @Override
    public int size() {
        return backingMap.size();
    }

    @Override
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return backingMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return backingMap.containsValue(value);
    }

    public V get(Object key) {
        ArrayList<V> currentValue = backingMap.get(key);
        if (currentValue == null || currentValue.size() < 1) {
            return null;
        }
        return currentValue.get(0);
    }


    public List<V> getList(K key) {
        List<V> values = new ArrayList<V>();
        ArrayList<V> currentValue = backingMap.get(key);
        if (currentValue == null) {
            return values;
        }
        for (V value : currentValue) {
            values.add(value);
        }
        return values;

    }

    @Override
    public V remove(Object key) {
        ArrayList<V> currentValue = backingMap.get(key);
        if (currentValue == null || currentValue.size() < 1) {
            return null;
        }
        backingMap.remove(key);
        return null;
    }


    @Override
    public void clear() {
        backingMap.clear();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (K key : m.keySet()) {
            put(key, m.get(key));
        }
    }

    @Override
    public Set<K> keySet() {
        HashSet<K> hashSet = new HashSet<K>();
        Set<K> ks = backingMap.keySet();
        for (K key : ks) {
            hashSet.add(key);
        }
        return hashSet;
    }

    @Override
    public Collection<V> values() {
        Collection<V> values = new ArrayList<V>();
        Set<K> ks = backingMap.keySet();
        for (K key : ks) {
            ArrayList<V> currentValue = backingMap.get(key);
            for (V value : currentValue) {
                values.add(value);
            }
        }
        return values;
    }

    @Override
    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K, V>> entries = new HashSet<Map.Entry<K, V>>();
        Set<K> ks = backingMap.keySet();
        for (K key : ks) {
            ArrayList<V> currentValue = backingMap.get(key);
            for (V value : currentValue) {
                Map.Entry<K, V> entry = new Entry<K, V>(key, value);
                entries.add(entry);
            }
        }
        return entries;
    }

    public class Entry<K,V> implements Map.Entry<K,V> {

        private K key;
        private V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            this.value = value;
            return null;
        }
    }

    public static void main(String[] args) {
        MapList<String, String> testingMap = new MapList<String, String>();
        testingMap.put("test1", "value1");
        testingMap.put("test1", "value2");
        System.out.println("Value1 = " + testingMap.get("test1"));
        List<String> stringList = testingMap.getList("test1");
        for (String string : stringList) {
            System.out.println("Value for key: test1" + string);
        }
        testingMap.remove("test1");
        System.out.println("Value1 = " + testingMap.get("test1"));
        testingMap.put("key1", "value1");
        testingMap.put("key1", "value2");
        testingMap.put("key2", "value2");
        testingMap.put("key3", "value3");
        testingMap.put("key4", "value4");
        testingMap.put("key5", "value5");
        Set<String> keySet = testingMap.keySet();
        for (String key : keySet) {
            System.out.println("key:" + key);
        }
        Set<Map.Entry<String, String>> entries = testingMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            System.out.println("Entry key:" + entry.getKey());
            System.out.println("Entry value:" + entry.getValue());
        }
    }
}

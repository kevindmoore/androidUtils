/** Copyright 2007, Master Tech Software, Inc, all rights reserved **/
package com.mastertechsoftware.util.list;

import java.util.*;

/**
 * <p>
 * This Map class is just a small class for useful methods.
 * </p>
 *
 * @author Kevin Moore
 */
public class OrderedMap<K,V> extends LinkedHashMap<K,V> {
    transient volatile Collection<K> keys = null;

    public OrderedMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public OrderedMap(int initialCapacity) {
        super(initialCapacity);
    }

    public OrderedMap() {
    }

    public OrderedMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public OrderedMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
    }

    public Object getKey(Object value) {
        for (Object key : keySet()) {
            Object hashValue = get(key);
            if (hashValue.equals(value)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Returns a collection view of the keys contained in this map.
     * This is a copy of the values method and used for keys.
     * Never understood why they didn't have this
     *
     * @return a collection view of the keys contained in this map.
     */
    public Collection<K> keys() {
        if (keys == null) {
            keys = new AbstractCollection<K>() {
                public Iterator<K> iterator() {
                    return new Iterator<K>() {
                        Iterator<K> iterator = keySet().iterator();

                        public boolean hasNext() {
                            return iterator.hasNext();
                        }

                        public K next() {
                            return iterator.next();
                        }

                        public void remove() {
                            iterator.remove();
                        }
                    };
                }

                public int size() {
                    return OrderedMap.this.size();
                }

                public boolean contains(Object v) {
                    return OrderedMap.this.containsValue(v);
                }
            };
        }
        return keys;
    }

}

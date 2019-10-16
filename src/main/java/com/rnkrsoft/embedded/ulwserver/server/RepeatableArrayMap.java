package com.rnkrsoft.embedded.ulwserver.server;

import java.util.*;

/**
 * Created by rnkrsoft.com on 2019/10/15.
 */
public class RepeatableArrayMap<K, V> implements RepeatableMap<K, V> {
    transient int size;
    RepeatableArrayEntry<K, V>[] table;

    volatile Collection<Entry<K, V>> entrySet;

    public RepeatableArrayMap(int initSize) {
        this.table = new RepeatableArrayEntry[initSize];
    }

    public RepeatableArrayMap() {
        this(16);
    }

    /**
     * 检测是否溢出
     *
     * @param index
     * @return
     */
    boolean checkOverflow(int index) {
        return table.length - 1 < index;
    }

    /**
     * 进行扩容
     *
     * @param newSize 新的大小
     */
    void expend(int newSize) {
        if (checkOverflow(size + 1)) {
            RepeatableArrayEntry<K, V>[] newTable = Arrays.copyOf(table, newSize);
            this.table = newTable;
        }
    }


    //---------------------------实现接口方法------------------------------------
    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int containsKey(K key) {
        int count = 0;
        for (int i = 0; i < size; i++) {
            RepeatableArrayEntry<K, V> entry = table[i];
            if (entry == null) {
                continue;
            }
            if (key.equals(entry.getKey())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int containsValue(V value) {
        int count = 0;
        for (int i = 0; i < size; i++) {
            RepeatableArrayEntry<K, V> entry = table[i];
            if (entry == null) {
                continue;
            }
            if (value.equals(entry.getValue())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Collection<V> get(K key) {
        return new ValuesCollection(key, false);
    }

    @Override
    public void put(K key, V value) {
        if (checkOverflow(size + 1)) {
            expend(size << 1);
        }
        this.table[size++] = new RepeatableArrayEntry<K, V>(key, value);
    }

    @Override
    public void set(K key, V value) {
        if (checkOverflow(size + 1)) {
            expend(size << 1);
        }
        boolean found = false;
        for (int i = 0; i < size; i++) {
            RepeatableArrayEntry<K, V> entry = table[i];
            if (entry == null) {
                continue;
            }
            if (key.equals(entry.getKey())) {
                entry.value = value;
                found = true;
                break;
            }
        }
        if (!found){
            this.table[size++] = new RepeatableArrayEntry<K, V>(key, value);
        }
    }

    @Override
    public V remove(K key) {
        for (int i = 0; i < size; i++) {
            RepeatableArrayEntry<K, V> entry = table[i];
            if (entry == null) {
                continue;
            }
            if (key.equals(entry.getKey())) {
                V value = entry.getValue();
                table[i] = null;
                return value;
            }
        }
        return null;
    }

    @Override
    public void putAll(RepeatableMap<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entries()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        this.size = 0;
        this.table = new RepeatableArrayEntry[4];
    }

    @Override
    public Collection<K> keys() {
        return new KeysCollection();
    }

    @Override
    public Collection<V> values() {
        return new ValuesCollection(null, true);
    }

    @Override
    public Collection<Entry<K, V>> entries() {
        Collection<Entry<K, V>> ec;
        return (ec = entrySet) == null ? (entrySet = new EntryCollection()) : ec;
    }

    final class KeysCollection extends AbstractCollection<K> {
        Object[] keys = new Object[table.length];
        int size0;

        public KeysCollection() {
            for (int i = 0; i < size; i++) {
                RepeatableArrayEntry<K, V> entry = table[i];
                if (entry == null) {
                    continue;
                }
                keys[size0] = entry.key;
                size0++;
            }
        }

        @Override
        public Iterator<K> iterator() {
            return new Iterator<K>() {
                int i;

                @Override
                public boolean hasNext() {
                    return i < size0;
                }

                @Override
                public K next() {
                    return (K) keys[i++];
                }
            };
        }

        @Override
        public int size() {
            return size0;
        }
    }

    final class ValuesCollection extends AbstractCollection<V> {
        Object[] values = new Object[table.length];
        int size0;

        public ValuesCollection(K key, boolean all) {
            for (int i = 0; i < size; i++) {
                RepeatableArrayEntry<K, V> entry = table[i];
                if (entry == null) {
                    continue;
                }
                if (all || key.equals(entry.getKey())) {
                    values[size0] = entry.value;
                    size0++;
                }
            }
        }

        @Override
        public Iterator<V> iterator() {
            return new Iterator<V>() {
                int i;

                @Override
                public boolean hasNext() {
                    return i < size0;
                }

                @Override
                public V next() {
                    return (V) values[i++];
                }
            };
        }

        @Override
        public int size() {
            return size0;
        }
    }

    final class EntryCollection extends AbstractCollection<Entry<K, V>> {

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return size;
        }
    }

    final class EntryIterator implements Iterator<Entry<K, V>> {
        int index;

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public Entry<K, V> next() {
            return table[index++];
        }
    }

    protected static class RepeatableArrayEntry<K, V> implements RepeatableMap.Entry<K, V> {
        public RepeatableArrayEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        K key;
        V value;


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

        @Override
        public String toString() {
            return "'" + key + "'->'" + value + "'";
        }
    }
}

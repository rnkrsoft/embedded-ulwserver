package com.rnkrsoft.embedded.httpserver.server;

import java.util.Collection;
import java.util.Map;

/**
 * Created by rnkrsoft.com on 2019/10/15.
 */
public interface RepeatableMap<K, V>{
    /**
     * 大小
     * @return
     */
    int size();

    /**
     * 是否为空
     * @return
     */
    boolean isEmpty();

    /**
     * 是否包含键
     * @param key
     * @return
     */
    int containsKey(K key);

    /**
     * 是否包含值
     * @param value
     * @return
     */
    int containsValue(V value);

    /**
     * 根据键获取值
     * @param key
     * @return
     */
    Collection<V> get(K key);

    /**
     * 将键值放入
     * @param key
     * @param value
     * @return
     */
    void put(K key, V value);

    /**
     * 找到键值符合的重新设置值
     * @param key
     * @param value
     */
    void set(K key, V value);
    /**
     * 移出键值
     * @param key
     * @return
     */
    V remove(K key);

    /**
     * 将一个可重复键值对放入
     * @param m
     */
    void putAll(RepeatableMap<? extends K, ? extends V> m);

    /**
     *
     * @param m
     */
    void putAll(Map<? extends K, ? extends V> m);

    /**
     * 清空
     */
    void clear();

    /**
     * 所有键
     * @return
     */
    Collection<K> keys();

    /**
     * 所有值
     * @return
     */
    Collection<V> values();

    /**
     * 遍历所有数据节点
     * @return
     */
    Collection<Entry<K, V>> entries();

    /**
     * 条目
     * @param <K>
     * @param <V>
     */
    interface Entry<K,V> {
        K getKey();

        V getValue();

        V setValue(V value);
    }
}

package com.chuanchen.util;

public class MapCacheValue<K, V> implements CacheValue<K,V> {
    private final K key;

    private final V value;

    private long createTime;

    private long maxLiveTime;

    private long maxIdleTime;

    private long lastAccessTime;

    public MapCacheValue(K key,V value,long maxLiveTime,long maxIdleTime){
        this.key = key;
        this.value = value;
        this.maxLiveTime = maxLiveTime;
        this.maxIdleTime = maxIdleTime;
        this.createTime = System.currentTimeMillis();
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        this.lastAccessTime = System.currentTimeMillis();
        return value;
    }

    public boolean isExpired() {
        if(this.maxLiveTime == 0L || this.maxIdleTime == 0L){
            return false;
        }else {
            long currentTime = System.currentTimeMillis();
            if(maxLiveTime != 0 && createTime + maxLiveTime < currentTime){
                return true;
            }
            return maxIdleTime != 0 && lastAccessTime + maxIdleTime > currentTime;
        }
    }
}

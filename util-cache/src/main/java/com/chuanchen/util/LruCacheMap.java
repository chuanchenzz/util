package com.chuanchen.util;

import java.util.HashSet;
import java.util.Set;

public class LruCacheMap<K, V> extends AbstractCacheMap<K, V> {
    private long maxLiveTime;
    private long maxIdleTime;
    private Set<CacheValue<K, V>> cacheValueSet = new HashSet<>();

    public LruCacheMap(int size, long maxLiveTime, long maxIdleTime) {
        super(size);
        this.maxIdleTime = maxIdleTime;
        this.maxLiveTime = maxLiveTime;
    }

    @Override
    protected void onValueRemove(CacheValue<K, V> cacheValue) {
        cacheValueSet.remove(cacheValue);
    }

    @Override
    protected void onValueRead(CacheValue<K, V> cacheValue) {
        if (cacheValue == null) {
            throw new NullPointerException();
        }
        cacheValueSet.remove(cacheValue);
        cacheValueSet.add(cacheValue);
    }

    @Override
    protected void onCreateValue(CacheValue<K, V> cacheValue) {
        cacheValueSet.add(cacheValue);
    }

    @Override
    protected void onMapFull() {
        for (CacheValue<K,V> cacheValue : cacheValueSet){
            if(cacheValue.isExpired()){
                cacheValueSet.remove(cacheValue);
                remove(cacheValue.getKey());
            }
        }
    }

    @Override
    protected CacheValue<K, V> createValue(K key, V value) {
        CacheValue<K, V> cacheValue = new MapCacheValue<>(key, value, maxLiveTime, maxIdleTime);
        return cacheValue;
    }
}

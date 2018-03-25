package com.chuanchen.util;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCacheMap<K, V> {
    private int maxSize;
    private Map<K, CacheValue<K, V>> map = new ConcurrentHashMap<>();

    public AbstractCacheMap(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size is invalid!");
        } else {
            this.maxSize = size;
        }
    }

    public int size() {
        return this.map.size();
    }

    public boolean isMapFull() {
        return size() >= maxSize;
    }

    public boolean isFull(K key) {
        if (size() == 0) {
            return false;
        } else if (size() >= this.size()) {
            return !this.map.containsKey(key);
        } else {
            return false;
        }
    }

    public boolean containsKey(K key) {
        if (key == null) {
            throw new NullPointerException();
        }
        CacheValue<K, V> cacheValue = this.map.get(key);
        if (cacheValue == null) {
            return false;
        } else if (isValueExpired(cacheValue)) {
            if (this.map.remove(key, cacheValue)) {
                this.onValueRemove(cacheValue);
                return false;
            } else {
                return this.containsKey(key);
            }

        } else {
            return true;
        }
    }

    public boolean containsValue(V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        Iterator<Map.Entry<K, CacheValue<K, V>>> iterator = this.map.entrySet().iterator();
        while (iterator.hasNext()) {
            CacheValue<K, V> cacheValue = iterator.next().getValue();
            if (cacheValue.getValue().equals(value)) {
                if (isValueExpired(cacheValue)) {
                    if (this.map.remove(cacheValue.getKey(), cacheValue)) {
                        this.onValueRemove(cacheValue);
                    }
                    return false;
                }
                this.onValueRead(cacheValue);
                return true;
            }
        }
        return false;
    }

    protected abstract void onValueRemove(CacheValue<K, V> cacheValue);

    protected abstract void onValueRead(CacheValue<K, V> cacheValue);

    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key is invalid!");
        }
        CacheValue<K, V> value = map.get(key);
        if (value == null) {
            return null;
        } else {
            if (!isValueExpired(value)) {
                this.onValueRead(value);
                return value.getValue();
            } else {
                if (this.map.remove(key, value)) {
                    this.onValueRemove(value);
                    return null;
                } else {
                    return this.get(key);
                }
            }
        }
    }

    public V put(K key, V value) {
        return this.put(key, value);
    }

    private V put(K key, V value, long maxLiveTime, long maxIdleTime) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("key or value is invalid!");
        }
        CacheValue<K, V> cacheValue = createValue(key, value);
        if (this.isFull(key) && !removeExpireEntries()){
            this.onMapFull();
        }
        this.onCreateValue(cacheValue);
        CacheValue<K,V> oldValue = this.map.put(key,cacheValue);
        if(oldValue != null){
            this.onValueRemove(oldValue);
            if(!this.isValueExpired(oldValue)){
                return oldValue.getValue();
            }
        }
        return null;
    }

    public boolean removeExpireEntries(){
        boolean removed = false;
        Iterator<Map.Entry<K,CacheValue<K,V>>> iterator = this.map.entrySet().iterator();
        while(iterator.hasNext()){
            CacheValue<K,V> cacheValue = iterator.next().getValue();
            if(this.isValueExpired(cacheValue) && this.map.remove(cacheValue.getKey(),cacheValue)){
                this.onValueRemove(cacheValue);
                removed = true;
            }
        }
        return removed;
    }

    protected abstract void onCreateValue(CacheValue<K,V> cacheValue);

    protected abstract void onMapFull();


    protected abstract CacheValue<K, V> createValue(K key, V value);

    public boolean isExpired(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key is invalid!");
        }
        CacheValue<K, V> value = map.get(key);
        if (value == null) {
            return true;
        } else {
            return isValueExpired(value);
        }
    }

    private boolean isValueExpired(CacheValue<K, V> value) {
        if (value.isExpired()) {
            return true;
        } else {
            return value.getValue() instanceof ExpireValue && ((ExpireValue) value.getValue()).isExpired();
        }
    }

    public V remove(K key){
        if(key == null){
            throw new NullPointerException();
        }
        CacheValue<K,V> cacheValue = this.map.remove(key);
        if(cacheValue != null){
            this.onValueRemove(cacheValue);
            if(!isValueExpired(cacheValue)) {
                return cacheValue.getValue();
            }
        }
        return null;
    }
    public void putAll(Map<K,CacheValue<K,V>> addMap){
        if(addMap == null){
            throw new NullPointerException();
        }
        Iterator<Map.Entry<K,CacheValue<K,V>>> iterator = addMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<K,CacheValue<K,V>> entry = iterator.next();
            this.map.put(entry.getKey(),entry.getValue());
        }
    }
}

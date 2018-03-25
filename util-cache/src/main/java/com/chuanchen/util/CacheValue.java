package com.chuanchen.util;

public interface CacheValue<K, V> extends ExpireValue {
    K getKey();

    V getValue();
}

package com.vietnl.sharedlibrary.data.cache;

public interface CacheStorePort {

  <K, V> V get(String namespace, K key, Class<V> clazz);

  <K, V> void put(String namespace, K key, V value);

  <K, V> void evict(String namespace, K key);

  <K, V> void evictByPrefix(String namespace, String prefix);
}

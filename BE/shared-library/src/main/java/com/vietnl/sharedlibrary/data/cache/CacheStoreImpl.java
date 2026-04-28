package com.vietnl.sharedlibrary.data.cache;

import org.hibernate.cache.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CacheStoreImpl implements CacheStorePort {

  private final CacheManager cacheManager;

  @Autowired
  public CacheStoreImpl(
      final CacheProperties properties, final List<CacheManager> cacheManagerList) {

    if (properties.getType() == null) {
      throw new CacheException("CacheManager not found!");
    }

    Optional<CacheManager> cacheManagerOptional =
        cacheManagerList.stream()
            .filter(item -> properties.getType().getCacheManagerClass().isInstance(item))
            .findFirst();

    if (cacheManagerOptional.isEmpty()) {
      throw new CacheException("CacheManager not found!");
    }

    cacheManager = cacheManagerOptional.get();
  }

  @Override
  public <K, V> V get(String namespace, K key, Class<V> clazz) {
    Cache cache = getCache(namespace);
    return cache.get(key, clazz);
  }

  @Override
  public <K, V> void put(String namespace, K key, V value) {
    Cache cache = getCache(namespace);
    cache.put(key, value);
  }

  @Override
  public <K, V> void evict(String namespace, K key) {
    Cache cache = getCache(namespace);
    cache.evict(key);
  }

  @Override
  public <K, V> void evictByPrefix(String namespace, String prefix) {
    Cache cache = getCache(namespace);
  }

  private Cache getCache(String namespace) {

    Cache cache = cacheManager.getCache(namespace);

    if (cache == null) {
      throw new CacheException(
          String.format("Cache namespace %s haven't configure yet!", namespace));
    }
    return cache;
  }
}

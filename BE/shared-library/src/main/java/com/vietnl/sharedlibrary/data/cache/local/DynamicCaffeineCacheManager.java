package com.vietnl.sharedlibrary.data.cache.local;

import com.eps.shared.data.cache.CacheProperties;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.util.concurrent.TimeUnit;

public class DynamicCaffeineCacheManager extends CaffeineCacheManager {

  private final CacheProperties properties;

  public DynamicCaffeineCacheManager(CacheProperties properties) {
    this.properties = properties;
  }

  @Override
  protected Cache createCaffeineCache(String name) {

    var namespace = properties.getNamespaces().get(name);

    long ttl =
        namespace != null
            ? coalesce(namespace.getTtl(), properties.getDefaultTTL())
            : properties.getDefaultTTL();

    long maxSize =
        namespace != null
            ? coalesce(namespace.getMaxSize(), properties.getDefaultMaxSize())
            : properties.getDefaultMaxSize();

    return new CaffeineCache(
        name,
        Caffeine.newBuilder()
            .expireAfterWrite(ttl, TimeUnit.MICROSECONDS)
            .maximumSize(maxSize)
            .build());
  }

  private static <T> T coalesce(T value, T fallback) {
    return value != null ? value : fallback;
  }
}

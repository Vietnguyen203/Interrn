package com.vietnl.sharedlibrary.data.cache;

import com.eps.shared.data.cache.local.DynamicCaffeineCacheManager;
import lombok.Getter;

@Getter
public enum CacheAdapterType {
  local(DynamicCaffeineCacheManager.class),
  redis(null);

  private final Class<?> cacheManagerClass;

  CacheAdapterType(Class<?> cacheManagerClass) {
    this.cacheManagerClass = cacheManagerClass;
  }
}

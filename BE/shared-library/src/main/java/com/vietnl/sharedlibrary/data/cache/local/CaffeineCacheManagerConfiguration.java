package com.vietnl.sharedlibrary.data.cache.local;

import com.eps.shared.data.cache.CacheProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Conditional(CaffeineCacheCondition.class)
public class CaffeineCacheManagerConfiguration {

  @Bean(name = "caffeineCacheManager")
  public CacheManager cacheManager(CacheProperties properties) {
    return new DynamicCaffeineCacheManager(properties);
  }

  public static <T> T coalesce(T value, T fallback) {
    return value != null ? value : fallback;
  }
}

package com.vietnl.sharedlibrary.data.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "platform.data.cache")
public class CacheProperties {

  private boolean enabled;
  private CacheAdapterType type;
  private long defaultTTL = 60000;
  private long defaultMaxSize = 10000;
  private Map<String, NamespaceInfo> namespaces = new HashMap<>();

  private Local local;
  private Redis redis;

  @Setter
  @Getter
  public static class NamespaceInfo {
    private Long ttl;
    private Long maxSize;
  }

  @Setter
  @Getter
  public static class Local {
    private boolean enabled;
  }

  @Setter
  @Getter
  public static class Redis {
    private boolean enabled;
  }
}

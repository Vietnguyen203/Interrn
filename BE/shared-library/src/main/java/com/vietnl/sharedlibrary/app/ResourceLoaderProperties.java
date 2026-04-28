package com.vietnl.sharedlibrary.app;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "platform.application.resourceloader")
public class ResourceLoaderProperties {

  private boolean enabled;
  private String url;
  private String resourceDir = "classpath*:resource_service/*.json";
}

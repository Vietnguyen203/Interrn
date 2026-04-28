package com.vietnl.sharedlibrary.observability.audit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "platform.observability.audit")
public class AuditProperties {
  private boolean enabled;
  String adapter;
  Http http;

  @Getter
  @Setter
  public static class Http {
    String url;
    String method = "POST";
  }
}

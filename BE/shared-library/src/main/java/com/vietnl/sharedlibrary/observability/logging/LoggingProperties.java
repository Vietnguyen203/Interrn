package com.vietnl.sharedlibrary.observability.logging;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Getter
@Setter
@ConfigurationProperties(prefix = "platform.observability.logging")
public class LoggingProperties {

  private boolean enabled;

  private Web web = new Web();

  @Data
  public static class Web {
    /** Whether the logging filter is enabled. */
    private boolean enabled = true;

    /** Maximum body size (in bytes) to capture. Larger bodies are truncated. */
    private int maxBodyBytes = 32 * 1024;

    /** URL path prefixes to exclude from logging (e.g. health checks, actuator). */
    private Set<String> excludedPathPrefixes =
        Set.of("/actuator", "/health", "/readiness", "/liveness");

    /** Header names (lowercase) whose values are masked in log output. */
    private Set<String> sensitiveHeaders =
        Set.of("authorization", "cookie", "set-cookie", "x-api-key", "x-auth-token");

    /** MIME types eligible for body capture. Non-matching types are skipped. */
    private Set<String> loggableContentTypes =
        Set.of(
            "application/json",
            "application/xml",
            "text/plain",
            "text/xml",
            "application/x-www-form-urlencoded",
            "application/problem+json");

    public boolean isExcludedPath(String path) {
      for (String prefix : getExcludedPathPrefixes()) {
        if (path.startsWith(prefix)) {
          return true;
        }
      }
      return false;
    }

    public boolean isLoggableContentType(String mediaType) {
      if (mediaType == null) {
        return false;
      }
      return loggableContentTypes.contains(mediaType);
    }
  }
}

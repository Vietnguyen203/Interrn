package com.vietnl.sharedlibrary.observability.audit.adapter;

import com.eps.shared.observability.audit.AuditLogEvent;
import com.eps.shared.observability.audit.AuditLogPort;
import com.eps.shared.observability.audit.AuditProperties;
import com.eps.shared.web.client.RequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "platform.observability.audit.adapter", havingValue = "http")
public class HTTPAuditLogAdapter implements AuditLogPort {

  private final AuditProperties properties;

  @Override
  public void log(AuditLogEvent event) {
    String httpMethod = properties.getHttp().getMethod().toUpperCase();
    String auditUrl = properties.getHttp().getUrl();
    try {
      HttpMethod method = HttpMethod.valueOf(httpMethod);
      RequestUtils.request(auditUrl, method, null, event, null)
          .subscribe(
              response -> log.debug("Audit log sent successfully: {}", response),
              error ->
                  log.error(
                      "Error sending audit log via RequestUtils to {}: {}",
                      auditUrl,
                      error.getMessage()));
    } catch (Exception e) {
      log.error("Error preparing audit log request for {}: {}", auditUrl, e.getMessage());
    }
  }
}

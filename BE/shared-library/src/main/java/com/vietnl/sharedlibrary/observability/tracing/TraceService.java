package com.vietnl.sharedlibrary.observability.tracing;

import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.security.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TraceService {

  public void trace(String method) {
    log.info(
        "[traceId = {} spanId = {} username = {} userId = {}] - {}",
        TraceContextHolder.getCurrentTraceId(),
        TraceContextHolder.getCurrentSpanId(),
        SecurityContextHolder.getUserCode(),
        SecurityContextHolder.extractClaims(HeaderContext::getUserId),
        method);
  }
}

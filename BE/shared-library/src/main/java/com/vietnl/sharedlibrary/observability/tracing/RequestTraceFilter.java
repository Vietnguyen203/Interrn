package com.vietnl.sharedlibrary.observability.tracing;

import com.eps.shared.core.constants.CommonConstant;
import com.eps.shared.core.constants.HeaderKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(CommonConstant.TRACE_ID_FILTER)
@RequiredArgsConstructor
public class RequestTraceFilter implements Filter {

  private final TraceService traceService;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    try {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      HttpServletResponse httpResponse = (HttpServletResponse) response;

      // Lấy traceId từ header nếu có, hoặc tự tạo
      String traceId = ((HttpServletRequest) request).getHeader(HeaderKeys.TRACE_ID);
      if (traceId == null || traceId.isEmpty()) {
        traceId = UUID.randomUUID().toString();
      }
      TraceContextHolder.setTraceId(traceId);

      // Gắn vào request attribute
      httpRequest.setAttribute(HeaderKeys.TRACE_ID, traceId);

      // Gắn vào response header để trả về client
      httpResponse.setHeader(HeaderKeys.TRACE_ID, traceId);

      traceService.trace("Start request");
      chain.doFilter(request, response);
    } finally {
      traceService.trace("End request");
      TraceContextHolder.clear();
    }
  }
}

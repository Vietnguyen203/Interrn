package com.vietnl.sharedlibrary.security.filter;

import com.eps.shared.core.constants.CommonConstant;
import com.eps.shared.core.constants.HeaderKeys;
import com.eps.shared.security.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(CommonConstant.SECURITY_FILTER_ORDER)
public class ContextExtractionFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;

    String user = httpRequest.getHeader(HeaderKeys.USER);

    try {
      SecurityContextHolder.set(user);
      chain.doFilter(request, response);
    } finally {
      SecurityContextHolder.clear();
    }
  }
}

package com.vietnl.sharedlibrary.core.constants;

import org.springframework.core.Ordered;

public class CommonConstant {

  private CommonConstant() {}

  public static final int SECURITY_FILTER_ORDER = Ordered.HIGHEST_PRECEDENCE;
  public static final int TRACE_ID_FILTER = SECURITY_FILTER_ORDER + 1;
  public static final int LOGGING_FILTER_ORDER = SECURITY_FILTER_ORDER + 1;
}

package com.vietnl.sharedlibrary.core.constants;

import org.springframework.http.HttpHeaders;

public class HeaderKeys {

  public static final String USER = "X-User";
  public static final String TRACE_ID = "X-Trace-Id";
  public static final String DEVICE_ID = "x-device-id";
  public static final String NAME = "X-User-Name";
  public static final String AUTHORIZATION = HttpHeaders.AUTHORIZATION.toLowerCase();
  public static final String BEARER = "Bearer ";
}

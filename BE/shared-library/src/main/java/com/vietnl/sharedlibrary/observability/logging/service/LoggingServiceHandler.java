package com.vietnl.sharedlibrary.observability.logging.service;

import com.eps.shared.observability.logging.LogEventType;

public interface LoggingServiceHandler extends LoggingService {

  LogEventType getType();
}

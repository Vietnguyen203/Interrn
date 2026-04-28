package com.vietnl.sharedlibrary.observability.logging.service;

import com.eps.shared.observability.logging.LogEntry;

public interface LoggingService {

  void logger(LogEntry entry);
}

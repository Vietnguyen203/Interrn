package com.vietnl.sharedlibrary.observability.logging.service;

import com.eps.shared.core.json.JsonParserUtils;
import com.eps.shared.observability.logging.LogEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingServiceOrchestrator implements LoggingService {

  @Override
  public void logger(LogEntry entry) {

    if (entry.isError()) {
      log.error(JsonParserUtils.toJson(entry));
    } else {
      log.info(JsonParserUtils.toJson(entry));
    }
  }
}

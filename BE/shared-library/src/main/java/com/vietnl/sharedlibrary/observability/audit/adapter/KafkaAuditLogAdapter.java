package com.vietnl.sharedlibrary.observability.audit.adapter;

import com.eps.shared.observability.audit.AuditLogEvent;
import com.eps.shared.observability.audit.AuditLogPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "platform.observability.audit.adapter", havingValue = "kafka")
public class KafkaAuditLogAdapter implements AuditLogPort {

  @Override
  public void log(AuditLogEvent event) {}
}

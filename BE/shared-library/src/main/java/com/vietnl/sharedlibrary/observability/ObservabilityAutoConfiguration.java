package com.vietnl.sharedlibrary.observability;

import com.eps.shared.observability.audit.AuditAutoConfiguration;
import com.eps.shared.observability.logging.LoggingAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AuditAutoConfiguration.class, LoggingAutoConfiguration.class})
public class ObservabilityAutoConfiguration {}

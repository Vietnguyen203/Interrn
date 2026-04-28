package com.vietnl.sharedlibrary.observability.audit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@ConditionalOnProperty(
    prefix = "platform.observability.audit",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false)
@ComponentScan("com.eps.shared.observability.audit")
@EnableConfigurationProperties(AuditProperties.class)
public class AuditAutoConfiguration {}

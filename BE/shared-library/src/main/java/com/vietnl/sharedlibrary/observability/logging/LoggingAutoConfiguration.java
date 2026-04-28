package com.vietnl.sharedlibrary.observability.logging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@ConditionalOnProperty(
    prefix = "platform.observability.logging",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false)
@ComponentScan("com.eps.shared.observability.logging")
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingAutoConfiguration {}

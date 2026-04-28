package com.vietnl.sharedlibrary.data.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
@EnableCaching
@ConditionalOnProperty(
    prefix = "platform.data.cache",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false)
@ComponentScan("com.eps.shared.data.cache")
public class CacheAutoConfiguration {}

package com.vietnl.sharedlibrary.data.jpa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan("com.eps.shared.data.jpa")
@Configuration
@EnableConfigurationProperties(JpaProperties.class)
@ConditionalOnProperty(
    prefix = "platform.data.jpa",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false)
public class JpaAutoConfiguration {}

package com.vietnl.sharedlibrary.app;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(ResourceLoaderProperties.class)
@Import({ResourceLoader.class})
public class ApplicationAutoConfiguration {}

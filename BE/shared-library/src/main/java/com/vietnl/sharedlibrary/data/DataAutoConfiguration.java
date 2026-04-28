package com.vietnl.sharedlibrary.data;

import com.eps.shared.data.cache.CacheAutoConfiguration;
import com.eps.shared.data.jpa.JpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CacheAutoConfiguration.class, JpaAutoConfiguration.class})
public class DataAutoConfiguration {}

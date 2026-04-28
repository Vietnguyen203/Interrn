package com.vietnl.sharedlibrary.data.jpa;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {

  @Bean
  public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(
      HibernateTracingInterceptor interceptor) {

    return props -> props.put("hibernate.session_factory.interceptor", interceptor);
  }
}

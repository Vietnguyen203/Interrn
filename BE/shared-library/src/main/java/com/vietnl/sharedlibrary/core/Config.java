package com.vietnl.sharedlibrary.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan("com.eps.shared.core")
public class Config {
  private static Environment environment;

  @Autowired
  public void setEnvironment(Environment env) {
    environment = env;
  }

  public static String getEnvironmentProperty(String key) {
    return environment.getProperty(key);
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}

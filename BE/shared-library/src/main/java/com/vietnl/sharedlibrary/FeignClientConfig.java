package com.vietnl.sharedlibrary;

import com.eps.shared.core.constants.HeaderKeys;
import com.eps.shared.core.exception.CommunicationResponseException;
import com.eps.shared.security.context.SecurityContextHolder;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.slf4j.MDC;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableFeignClients
public class FeignClientConfig {

  @Bean
  public RequestInterceptor requestInterceptor() {
    return new RequestInterceptor() {
      @Override
      public void apply(RequestTemplate template) {
        template.header(HeaderKeys.TRACE_ID, MDC.get(HeaderKeys.TRACE_ID));
        template.header(HeaderKeys.USER, SecurityContextHolder.getXUser());
        //
        //        traceService.trace(
        //            String.format(
        //                "Call to url %s - method %s",
        //                template.request().url(), template.request().httpMethod()));
      }
    };
  }

  @Bean
  public ErrorDecoder decode() {
    return (s, response) -> {
      try {
        return new CommunicationResponseException(
            HttpStatus.valueOf(response.status()),
            response.body() == null
                ? null
                : Util.toString(response.body().asReader(StandardCharsets.UTF_8)));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };
  }

  @Bean
  Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL; // Phải FULL để log body
  }
}

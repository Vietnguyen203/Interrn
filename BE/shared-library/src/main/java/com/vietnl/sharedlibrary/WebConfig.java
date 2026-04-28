package com.vietnl.sharedlibrary;

import com.eps.shared.web.interceptors.InterceptorFactory;
import com.eps.shared.web.method.argument.MethodArgumentFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final InterceptorFactory interceptor;
  private final MethodArgumentFactory methodArgument;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    WebMvcConfigurer.super.addArgumentResolvers(resolvers);
    resolvers.addAll(methodArgument.getMethodArgumentList());
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {}

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    interceptor
        .getMethodArgumentList()
        .forEach(interceptor -> registry.addInterceptor(interceptor).addPathPatterns("/**"));
  }
}

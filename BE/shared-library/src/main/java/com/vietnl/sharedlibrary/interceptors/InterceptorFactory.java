package com.vietnl.sharedlibrary.interceptors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Getter
@Component
public class InterceptorFactory {

  public final List<IInterceptor> methodArgumentList;
}

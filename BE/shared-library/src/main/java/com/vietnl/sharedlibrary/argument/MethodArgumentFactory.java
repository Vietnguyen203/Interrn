package com.vietnl.sharedlibrary.argument;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Getter
@Component
public class MethodArgumentFactory {

  public final List<IMethodArgument> methodArgumentList;
}

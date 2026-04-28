package com.vietnl.sharedlibrary;

import feign.Logger;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service(value = "feignLogger")
@RequiredArgsConstructor
public class JsonFeignLogger extends Logger {

  @Override
  protected void log(String configKey, String format, Object... args) {}

  @Override
  protected Response logAndRebufferResponse(
      String configKey, Level logLevel, Response response, long elapsedTime) throws IOException {
    return response;
  }
}

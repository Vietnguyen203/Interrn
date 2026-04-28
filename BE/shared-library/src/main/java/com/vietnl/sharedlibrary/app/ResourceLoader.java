package com.vietnl.sharedlibrary.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "platform.application.resourceloader.enabled", havingValue = "true")
public class ResourceLoader {

  int attempts = 0;

  private final RestTemplate restTemplate;
  private final ResourceLoaderProperties properties;

  /**
   * Gọi khi ứng dụng Spring Boot đã khởi động thành công. Đọc tất cả file JSON từ thư mục
   * resource_service/ - Kết hợp nội dung thành 1 mảng JSON Gửi đến resource-service qua HTTP POST
   */
  @EventListener(ApplicationReadyEvent.class)
  @Async
  @Retryable(
      retryFor = {Exception.class},
      maxAttempts = Integer.MAX_VALUE, // UNLIMITED như Eureka
      backoff =
          @Backoff(
              delay = 30000, // 30s
              multiplier = 2.0, // Exponential: 30s, 60s, 120s, 300s, 300s...
              maxDelay = 300000 // Max 5 phút, sau đó cố định 5 phút
              ))
  public void onApplicationEvent() throws Exception {
    if (!StringUtils.hasText(properties.getUrl())) {
      return;
    }
    Resource[] resources = loadJsonResources();
    for (Resource resource : resources) {
      sendDataToResourceService(properties.getUrl(), resource);
    }
    attempts++;
    if (attempts % 10 == 0) {}
  }

  private void sendDataToResourceService(String url, Resource resource) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Resource> entity = new HttpEntity<>(resource, headers);

    // Gửi POST request
    restTemplate.postForEntity(url, entity, Void.class);
  }

  private Resource[] loadJsonResources() throws Exception {
    return new PathMatchingResourcePatternResolver().getResources(properties.getResourceDir());
  }
}

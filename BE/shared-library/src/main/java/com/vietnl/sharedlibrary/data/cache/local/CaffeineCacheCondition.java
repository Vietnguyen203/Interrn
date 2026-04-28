package com.vietnl.sharedlibrary.data.cache.local;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class CaffeineCacheCondition extends AnyNestedCondition {
  public CaffeineCacheCondition() {
    // Thiết lập phase: REGISTER_BEAN (thường dùng nhất) hoặc PARSE_CONFIGURATION
    super(ConfigurationPhase.REGISTER_BEAN);
  }

  // Điều kiện 1: Kiểm tra property feature.a.enabled
  @ConditionalOnProperty(
      prefix = "platform.data.cache",
      name = "type",
      havingValue = "local",
      matchIfMissing = false)
  static class ConditionA {}

  // Điều kiện 2: Kiểm tra property feature.b.enabled
  @ConditionalOnProperty(
      prefix = "platform.data.cache.local",
      name = "enabled",
      havingValue = "true")
  static class ConditionB {}
}

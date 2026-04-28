package com.vietnl.sharedlibrary.core.cqrs.command;

import com.eps.shared.core.aware.ResolvableTypeAware;
import org.springframework.core.ResolvableType;

public interface Command<R> extends ResolvableTypeAware {

  // Phương thức này sẽ tự động hóa việc lấy ResolvableType
  @Override
  default ResolvableType getResolvableType() {
    return ResolvableType.forInstance(this);
  }
}

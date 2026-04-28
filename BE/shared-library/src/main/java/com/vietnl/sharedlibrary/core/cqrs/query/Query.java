package com.vietnl.sharedlibrary.core.cqrs.query;

import com.eps.shared.core.aware.ResolvableTypeAware;
import org.springframework.core.ResolvableType;

public interface Query<R> extends ResolvableTypeAware {

  // Phương thức này sẽ tự động hóa việc lấy ResolvableType tương tự Command
  @Override
  default ResolvableType getResolvableType() {
    return ResolvableType.forInstance(this);
  }
}

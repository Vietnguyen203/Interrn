package com.vietnl.sharedlibrary.core.services.v2;

import com.eps.shared.core.context.HeaderContext;

public interface CreateService<C, R> {
  R create(HeaderContext context, C request);
}

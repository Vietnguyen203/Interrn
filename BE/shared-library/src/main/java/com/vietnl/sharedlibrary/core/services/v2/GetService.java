package com.vietnl.sharedlibrary.core.services.v2;

import com.eps.shared.core.context.HeaderContext;

public interface GetService<ID, R> {
  R getById(HeaderContext context, ID id);
}

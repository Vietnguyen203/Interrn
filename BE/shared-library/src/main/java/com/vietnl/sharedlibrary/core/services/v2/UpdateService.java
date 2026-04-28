package com.vietnl.sharedlibrary.core.services.v2;

import com.eps.shared.core.context.HeaderContext;

public interface UpdateService<ID, C, R> {
  R update(HeaderContext context, ID id, C request);
}

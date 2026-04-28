package com.vietnl.sharedlibrary.core.services.v2;

import com.eps.shared.core.context.HeaderContext;

public interface PatchService<ID, C, R> {
  R patch(HeaderContext context, ID id, C request);
}

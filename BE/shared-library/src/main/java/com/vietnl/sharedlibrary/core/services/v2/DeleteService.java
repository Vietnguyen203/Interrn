package com.vietnl.sharedlibrary.core.services.v2;

import com.eps.shared.core.context.HeaderContext;
import java.util.List;

public interface DeleteService<ID> {
  void delete(HeaderContext context, ID id);

  void deleteBatch(HeaderContext context, List<ID> ids);
}

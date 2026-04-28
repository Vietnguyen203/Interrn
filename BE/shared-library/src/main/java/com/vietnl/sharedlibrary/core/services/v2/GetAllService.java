package com.vietnl.sharedlibrary.core.services.v2;

import com.eps.shared.core.context.HeaderContext;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface GetAllService<R> {
  Page<R> getAll(
      HeaderContext context,
      String search,
      Integer page,
      Integer pageSize,
      String sort,
      Map<String, Object> filter);
}

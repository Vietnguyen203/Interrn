package com.vietnl.sharedlibrary.core.cqrs.query;

import com.eps.shared.core.context.HeaderContext;

public interface QueryMiddleware {
  <R> R handle(HeaderContext context, Query<R> query, QueryHandlerDelegate<R> next);
}

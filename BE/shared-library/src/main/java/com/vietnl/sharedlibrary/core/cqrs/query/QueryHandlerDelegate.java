package com.vietnl.sharedlibrary.core.cqrs.query;

import com.eps.shared.core.context.HeaderContext;

@FunctionalInterface
public interface QueryHandlerDelegate<R> {
  R handle(HeaderContext context, Query<R> query);
}

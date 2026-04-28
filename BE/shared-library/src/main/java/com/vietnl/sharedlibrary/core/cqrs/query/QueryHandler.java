package com.vietnl.sharedlibrary.core.cqrs.query;

import com.eps.shared.core.context.HeaderContext;

public interface QueryHandler<Q extends Query<R>, R> {

  R handle(HeaderContext context, Q query);
}

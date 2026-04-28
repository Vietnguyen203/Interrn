package com.vietnl.sharedlibrary.core.cqrs.query;

import com.eps.shared.core.context.HeaderContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface GetListQueryHandler<E, ID, R>
    extends QueryHandler<GetListQuery<E, ID, R>, List<R>> {

  @Override
  @Transactional(readOnly = true)
  List<R> handle(HeaderContext context, GetListQuery<E, ID, R> query);
}

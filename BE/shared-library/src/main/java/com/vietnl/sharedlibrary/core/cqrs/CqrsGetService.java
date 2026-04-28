package com.vietnl.sharedlibrary.core.cqrs;

import com.eps.shared.core.aware.EntityAware;
import com.eps.shared.core.aware.ResultAware;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.query.GetQuery;
import com.eps.shared.core.services.v2.GetService;

public interface CqrsGetService<E, ID, R>
    extends MediatorAware, EntityAware<E>, ResultAware<R>, GetService<ID, R> {

  @Override
  default R getById(HeaderContext context, ID id) {
    return (R) getMediator().query(context, new GetQuery<>(getEntityClass(), id, getResultClass()));
  }
}

package com.vietnl.sharedlibrary.core.cqrs;

import com.eps.shared.core.aware.EntityAware;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.command.CreateCommand;
import com.eps.shared.core.services.v2.CreateService;

public interface CqrsCreateService<E, C, R>
    extends MediatorAware, EntityAware<E>, CreateService<C, R> {

  @Override
  default R create(HeaderContext context, C request) {
    return (R) getMediator().send(context, new CreateCommand<>(getEntityClass(), request));
  }
}

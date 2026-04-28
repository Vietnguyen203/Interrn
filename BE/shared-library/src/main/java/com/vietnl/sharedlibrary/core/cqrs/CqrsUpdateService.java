package com.vietnl.sharedlibrary.core.cqrs;

import com.eps.shared.core.aware.EntityAware;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.command.UpdateCommand;
import com.eps.shared.core.services.v2.UpdateService;

public interface CqrsUpdateService<E, ID, C, R>
    extends MediatorAware, EntityAware<E>, UpdateService<ID, C, R> {

  @Override
  default R update(HeaderContext context, ID id, C request) {
    return (R) getMediator().send(context, new UpdateCommand<>(getEntityClass(), id, request));
  }
}

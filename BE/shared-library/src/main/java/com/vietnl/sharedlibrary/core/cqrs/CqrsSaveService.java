package com.vietnl.sharedlibrary.core.cqrs;

import com.eps.shared.core.aware.EntityAware;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.command.SaveCommand;

public interface CqrsSaveService<E, D> extends MediatorAware, EntityAware<E> {

  default Object save(HeaderContext context, D data) {
    return getMediator().send(context, new SaveCommand<>(getEntityClass(), data));
  }
}

package com.vietnl.sharedlibrary.core.cqrs;

import com.eps.shared.core.aware.EntityAware;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.command.BulkDeleteCommand;

public interface CqrsBulkDeleteService<E, ID> extends MediatorAware, EntityAware<E> {

  default Object bulkDelete(HeaderContext context, Iterable<ID> ids) {
    return getMediator().send(context, new BulkDeleteCommand<>(getEntityClass(), ids));
  }
}

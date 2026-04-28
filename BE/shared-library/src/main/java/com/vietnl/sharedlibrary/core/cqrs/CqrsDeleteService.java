package com.vietnl.sharedlibrary.core.cqrs;

import com.eps.shared.core.aware.EntityAware;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.cqrs.command.BulkDeleteCommand;
import com.eps.shared.core.cqrs.command.DeleteCommand;
import com.eps.shared.core.services.v2.DeleteService;
import java.util.List;

public interface CqrsDeleteService<E, ID> extends MediatorAware, EntityAware<E>, DeleteService<ID> {

  @Override
  default void delete(HeaderContext context, ID id) {
    getMediator().send(context, new DeleteCommand<>(getEntityClass(), id));
  }

  @Override
  default void deleteBatch(HeaderContext context, List<ID> ids) {
    getMediator().send(context, new BulkDeleteCommand<>(getEntityClass(), ids));
  }
}

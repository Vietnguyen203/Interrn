package com.vietnl.sharedlibrary.core.cqrs.command;

import com.eps.shared.core.aware.ICrudPersistenceProvider;
import com.eps.shared.core.context.HeaderContext;
import jakarta.transaction.Transactional;

public interface DeleteCommandHandler<E, ID>
    extends CommandHandler<DeleteCommand<E, ID>, Void>, ICrudPersistenceProvider<E, ID> {

  @Override
  @Transactional
  default Void handle(HeaderContext context, DeleteCommand<E, ID> command) {
    if (getCrudPersistence() == null) {
      throw new IllegalArgumentException("crudPersistence must not be null");
    }

    validate(context, command.getId());
    getCrudPersistence().deleteById(command.getId());
    postHandler(context, command.getId());

    return null;
  }

  default void validate(HeaderContext context, ID id) {}

  default void postHandler(HeaderContext context, ID id) {}
}

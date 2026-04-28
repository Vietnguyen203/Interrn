package com.vietnl.sharedlibrary.core.cqrs.command;

import com.eps.shared.core.aware.ICrudPersistenceProvider;
import com.eps.shared.core.context.HeaderContext;

public interface BulkDeleteCommandHandler<E, ID>
    extends CommandHandler<BulkDeleteCommand<E, ID>, Void>, ICrudPersistenceProvider<E, ID> {

  //
  //  @Override
  //  @Transactional
  //  default void handle(HeaderContext context, BulkDeleteCommand<E, Void, ID> command) {
  //    if (getCrudPersistence() == null) {
  //      throw new IllegalArgumentException("crudPersistence must not be null");
  //    }
  //
  //    validate(context, command.getIds());
  //    getCrudPersistence().deleteAllByIdIn(command.getIds());
  //    postHandler(context, command.getIds());
  //
  //    return mappingResult(context, command.getIds());
  //  }

  @Override
  default Void handle(HeaderContext context, BulkDeleteCommand<E, ID> command) {
    if (getCrudPersistence() == null) {
      throw new IllegalArgumentException("crudPersistence must not be null");
    }

    validate(context, command.getIds());
    getCrudPersistence().deleteAllByIdIn(command.getIds());
    postHandler(context, command.getIds());

    return null;
  }

  default void validate(HeaderContext context, Iterable<ID> ids) {}

  default void postHandler(HeaderContext context, Iterable<ID> ids) {}
}

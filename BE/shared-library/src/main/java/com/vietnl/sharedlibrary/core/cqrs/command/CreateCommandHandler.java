package com.vietnl.sharedlibrary.core.cqrs.command;

import com.eps.shared.core.aware.ICrudPersistenceProvider;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.mapper.FnCommon;
import com.eps.shared.core.utils.GenerationUtils;
import jakarta.transaction.Transactional;

public interface CreateCommandHandler<E, D, ID>
    extends CommandHandler<CreateCommand<E, D>, Object>, ICrudPersistenceProvider<E, ID> {
  @Override
  @Transactional
  default Object handle(HeaderContext context, CreateCommand<E, D> command) {
    if (getCrudPersistence() == null) {
      throw new IllegalArgumentException("createPersistence must not be null");
    }

    E entity = GenerationUtils.newInstance(command.getEntityClass());

    validate(context, entity, command.getData());
    mappingEntity(context, entity, command.getData());
    entity = getCrudPersistence().save(entity);
    postHandler(context, entity, command.getData());

    return mappingResult(context, entity, command.getData());
  }

  default void validate(HeaderContext headerContext, E entity, D command) {}

  default void mappingEntity(HeaderContext context, E entity, D command) {
    FnCommon.copyAllProperties(entity, command);
  }

  default void postHandler(HeaderContext context, E entity, D command) {}

  default Object mappingResult(HeaderContext context, E entity, D command) {
    return entity;
  }
}

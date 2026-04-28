package com.vietnl.sharedlibrary.core.cqrs.command;

import com.eps.shared.core.aware.ICrudPersistenceProvider;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.mapper.FnCommon;
import com.eps.shared.core.utils.GenerationUtils;
import jakarta.transaction.Transactional;

public interface SaveCommandHandler<E, D, ID>
    extends CommandHandler<SaveCommand<E, D>, Object>, ICrudPersistenceProvider<E, ID> {

  @Override
  @Transactional
  default Object handle(HeaderContext context, SaveCommand<E, D> command) {
    if (getCrudPersistence() == null) {
      throw new IllegalArgumentException("crudPersistence must not be null");
    }

    E entity = getEntity(command.getEntityClass());
    entity = validate(context, entity, command.getData());
    entity = mappingEntity(context, entity, command.getData());
    entity = saveEntity(entity);
    entity = postHandler(context, entity, command.getData());

    return mappingResult(context, entity, command.getData());
  }

  default E getEntity(Class<E> entityClass) {
    return getCrudPersistence()
        .findFirstByOrderByIdDesc()
        .orElse(GenerationUtils.newInstance(entityClass));
  }

  default E validate(HeaderContext context, E entity, D commandData) {
    return entity;
  }

  default E mappingEntity(HeaderContext context, E entity, D commandData) {
    FnCommon.copyAllProperties(entity, commandData);
    return entity;
  }

  default E saveEntity(E entity) {
    return getCrudPersistence().save(entity);
  }

  default E postHandler(HeaderContext context, E entity, D commandData) {
    return entity;
  }

  default Object mappingResult(HeaderContext context, E entity, D commandData) {
    return entity;
  }
}

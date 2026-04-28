package com.vietnl.sharedlibrary.core.cqrs.command;

import com.eps.shared.core.aware.ICrudPersistenceProvider;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.exception.CommonErrorMessage;
import com.eps.shared.core.exception.ResponseException;
import com.eps.shared.core.mapper.FnCommon;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;

public interface PatchCommandHandler<E, ID, D>
    extends CommandHandler<PatchCommand<E, ID, D>, Object>, ICrudPersistenceProvider<E, ID> {

  @Override
  @Transactional
  default Object handle(HeaderContext context, PatchCommand<E, ID, D> command) {
    if (getCrudPersistence() == null) {
      throw new IllegalArgumentException("crudPersistence must not be null");
    }

    E entity = getCrudPersistence().findByIdOrNull(command.getId());

    if (entity == null) {
      throw new ResponseException(HttpStatus.NOT_FOUND, CommonErrorMessage.NOT_FOUND);
    }

    validate(context, entity, command.getData());
    mappingEntity(context, entity, command.getData());
    entity = getCrudPersistence().save(entity);
    postHandler(context, entity, command.getData());

    return mappingResult(context, entity, command.getData());
  }

  default void validate(HeaderContext context, E entity, D commandData) {}

  default void mappingEntity(HeaderContext context, E entity, D commandData) {
    FnCommon.copyNotNullProperties(entity, commandData);
  }

  default void postHandler(HeaderContext context, E entity, D commandData) {}

  default Object mappingResult(HeaderContext context, E entity, D commandData) {
    return entity;
  }
}

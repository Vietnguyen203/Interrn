package com.vietnl.sharedlibrary.core.cqrs.query;

import com.eps.shared.core.aware.ICrudPersistenceProvider;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.mapper.FnCommon;
import org.springframework.transaction.annotation.Transactional;

public interface GetQueryHandler<E, ID, R>
    extends QueryHandler<GetQuery<E, ID, R>, R>, ICrudPersistenceProvider<E, ID> {

  @Override
  @Transactional(readOnly = true)
  default R handle(HeaderContext context, GetQuery<E, ID, R> query) {
    if (getCrudPersistence() == null) {
      throw new IllegalArgumentException("crudPersistence must not be null");
    }

    E entity = getEntity(query.getId());
    entity = validate(context, entity);
    entity = postHandler(context, entity);

    return mappingResult(context, entity, query.getResultClass());
  }

  default E getEntity(ID id) {
    E entity = getCrudPersistence().findByIdOrNull(id);
    if (entity == null) {
      throw new IllegalArgumentException("Entity not found with ID: " + id);
    }
    return entity;
  }

  default E validate(HeaderContext context, E entity) {
    return entity;
  }

  default E postHandler(HeaderContext context, E entity) {
    return entity;
  }

  default R mappingResult(HeaderContext context, E entity, Class<R> resultClass) {
    return FnCommon.copyProperties(resultClass, entity);
  }
}

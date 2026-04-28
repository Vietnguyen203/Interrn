package com.vietnl.sharedlibrary.core.cqrs.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.ResolvableType;

@AllArgsConstructor
@Getter
public class GetQuery<E, ID, R> implements Query<R> {
  private final Class<E> entityClass;

  private final ID id;
  private final Class<R> resultClass;

  @Override
  public ResolvableType getResolvableType() {
    return ResolvableType.forClassWithGenerics(
        GetQuery.class,
        ResolvableType.forClass(entityClass),
        ResolvableType.forInstance(id),
        ResolvableType.forClass(resultClass));
  }
}

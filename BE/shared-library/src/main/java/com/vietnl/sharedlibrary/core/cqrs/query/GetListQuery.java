package com.vietnl.sharedlibrary.core.cqrs.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.ResolvableType;

import java.util.List;

@AllArgsConstructor
@Getter
public class GetListQuery<E, ID, R> implements Query<List<R>> {
  private final Class<E> entityClass;

  private final ID id;
  private final Class<R> resultClass;

  @Override
  public ResolvableType getResolvableType() {
    return ResolvableType.forClassWithGenerics(
        GetListQuery.class,
        ResolvableType.forClass(entityClass),
        ResolvableType.forInstance(id),
        ResolvableType.forClass(resultClass));
  }
}

package com.vietnl.sharedlibrary.core.cqrs.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;

import java.util.Map;

@AllArgsConstructor
@Getter
public class SearchQuery<E, R> implements Query<Page<R>> {
  private final Class<E> entityClass;

  private String search;
  private Integer page;
  private Integer pageSize;
  private String sort;
  private Map<String, Object> filter;

  private final Class<R> resultClass;

  @Override
  public ResolvableType getResolvableType() {
    return ResolvableType.forClassWithGenerics(
        SearchQuery.class,
        ResolvableType.forClass(entityClass),
        ResolvableType.forClass(resultClass));
  }
}

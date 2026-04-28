package com.vietnl.sharedlibrary.core.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.ResolvableType;

@AllArgsConstructor
@Getter
public class CreateCommand<E, D> implements Command<Object> {

  private final Class<E> entityClass;

  private final D data;

  //  private final Class<R> resultClass;

  @Override
  public ResolvableType getResolvableType() {
    // Tái cấu trúc type từ entityType có sẵn trong record
    return ResolvableType.forClassWithGenerics(
        CreateCommand.class,
        ResolvableType.forClass(entityClass),
        ResolvableType.forInstance(data));
  }
}

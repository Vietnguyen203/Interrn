package com.vietnl.sharedlibrary.core.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.ResolvableType;

@AllArgsConstructor
@Getter
public class PatchCommand<E, ID, D> implements Command<Object> {

  private final Class<E> entityClass;

  private final ID id;

  private final D data;

  @Override
  public ResolvableType getResolvableType() {
    return ResolvableType.forClassWithGenerics(
        PatchCommand.class,
        ResolvableType.forClass(entityClass),
        ResolvableType.forInstance(id),
        ResolvableType.forInstance(data));
  }
}

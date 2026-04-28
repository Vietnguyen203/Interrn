package com.vietnl.sharedlibrary.core.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.ResolvableType;

@AllArgsConstructor
@Getter
public class SaveCommand<E, D> implements Command<Object> {

  private final Class<E> entityClass;

  private final D data;

  @Override
  public ResolvableType getResolvableType() {
    return ResolvableType.forClassWithGenerics(
        SaveCommand.class, ResolvableType.forClass(entityClass), ResolvableType.forInstance(data));
  }
}

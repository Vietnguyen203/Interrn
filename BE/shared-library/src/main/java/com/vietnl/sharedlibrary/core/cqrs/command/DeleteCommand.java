package com.vietnl.sharedlibrary.core.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.ResolvableType;

@AllArgsConstructor
@Getter
public class DeleteCommand<E, ID> implements Command<Void> {
  private final Class<E> entityClass;
  private final ID id;

  @Override
  public ResolvableType getResolvableType() {
    return ResolvableType.forClassWithGenerics(
        DeleteCommand.class, ResolvableType.forClass(entityClass), ResolvableType.forInstance(id));
  }
}

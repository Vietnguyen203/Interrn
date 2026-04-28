package com.vietnl.sharedlibrary.core.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.ResolvableType;

@AllArgsConstructor
@Getter
public class BulkDeleteCommand<E, ID> implements Command<Void> {

  private final Class<E> entityClass;

  private final Iterable<ID> ids;

  @Override
  public ResolvableType getResolvableType() {
    return ResolvableType.forClassWithGenerics(
        BulkDeleteCommand.class,
        ResolvableType.forClass(entityClass),
        ResolvableType.forInstance(ids));
  }
}

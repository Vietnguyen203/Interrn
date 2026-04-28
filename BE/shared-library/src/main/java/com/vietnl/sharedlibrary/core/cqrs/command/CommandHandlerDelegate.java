package com.vietnl.sharedlibrary.core.cqrs.command;

import com.eps.shared.core.context.HeaderContext;

@FunctionalInterface
public interface CommandHandlerDelegate<R> {
  R handle(HeaderContext context, Command<R> command);
}

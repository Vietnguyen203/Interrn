package com.vietnl.sharedlibrary.core.cqrs.command;

import com.eps.shared.core.context.HeaderContext;

public interface CommandMiddleware {
  <R> R handle(HeaderContext context, Command<R> command, CommandHandlerDelegate<R> next);
}

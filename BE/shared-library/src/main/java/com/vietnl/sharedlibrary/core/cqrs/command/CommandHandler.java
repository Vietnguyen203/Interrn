package com.vietnl.sharedlibrary.core.cqrs.command;

import com.eps.shared.core.context.HeaderContext;

public interface CommandHandler<C extends Command<R>, R> {

  R handle(HeaderContext context, C command);
}

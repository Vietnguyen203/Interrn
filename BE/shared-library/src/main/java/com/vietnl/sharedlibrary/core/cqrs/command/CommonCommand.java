package com.vietnl.sharedlibrary.core.cqrs.command;

import com.eps.shared.core.aware.ResultAware;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public abstract class CommonCommand<D, R> implements Command<R>, ResultAware<R> {

  private final D data;
}

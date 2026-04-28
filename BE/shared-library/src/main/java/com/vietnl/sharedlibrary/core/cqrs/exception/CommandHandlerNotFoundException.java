package com.vietnl.sharedlibrary.core.cqrs.exception;

public class CommandHandlerNotFoundException extends RuntimeException {
  public CommandHandlerNotFoundException(String message) {
    super(message);
  }
}

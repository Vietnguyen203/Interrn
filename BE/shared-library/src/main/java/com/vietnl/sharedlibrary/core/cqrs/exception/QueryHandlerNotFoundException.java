package com.vietnl.sharedlibrary.core.cqrs.exception;

public class QueryHandlerNotFoundException extends RuntimeException {
  public QueryHandlerNotFoundException(String message) {
    super(message);
  }
}

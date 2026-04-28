package com.vietnl.sharedlibrary.core.valueobject;

import lombok.Getter;

@Getter
public enum PositionType {
  FIRST(0),
  LAST(-1);

  private final int value;

  PositionType(final int value) {
    this.value = value;
  }
}

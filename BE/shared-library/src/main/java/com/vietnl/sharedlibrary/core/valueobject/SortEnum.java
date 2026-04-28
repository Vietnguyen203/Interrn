package com.vietnl.sharedlibrary.core.valueobject;

import lombok.Getter;

@Getter
public enum SortEnum {
  desc(-1),
  asc(1);
  private final int original;

  SortEnum(int original) {
    this.original = original;
  }

  public static SortEnum fromOriginal(int original) {

    for (SortEnum sortEnum : SortEnum.values()) {
      if (sortEnum.original == original) {
        return sortEnum;
      }
    }

    throw new IllegalArgumentException("Invalid sort value: " + original);
  }
}

package com.vietnl.sharedlibrary.data.jpa.query.interfaces;

public interface Aliasable<T> {

  T as(String alias);
}

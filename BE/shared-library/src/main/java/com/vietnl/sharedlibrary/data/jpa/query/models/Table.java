package com.vietnl.sharedlibrary.data.jpa.query.models;

import com.eps.shared.data.jpa.query.interfaces.Aliasable;
import com.eps.shared.data.jpa.query.interfaces.ImmutableTable;
import org.springframework.util.StringUtils;

public class Table implements ImmutableTable, Aliasable<ImmutableTable> {

  private final String name;
  private String alias;

  protected Table(String name) {
    this.name = name;
  }

  @Override
  public ImmutableTable as(String alias) {
    this.alias = alias;
    return this;
  }

  @Override
  public String getTableName() {
    return StringUtils.hasText(alias) ? alias : name;
  }

  @Override
  public String getName() {
    return StringUtils.hasText(alias) ? String.format("%s as %s", name, alias) : name;
  }
}

package com.vietnl.sharedlibrary.data.jpa.query.models;

import com.eps.shared.data.jpa.query.interfaces.Aliasable;
import com.eps.shared.data.jpa.query.interfaces.ImmutableColumn;
import org.springframework.util.StringUtils;

import java.util.Collection;

public class Column<T> implements Aliasable<ImmutableColumn>, ImmutableColumn {

  private final String name;
  private String alias;

  protected Column(String name) {
    this.name = name;
  }

  @Override
  public ImmutableColumn as(String alias) {
    this.alias = alias;
    return this;
  }

  @Override
  public String getName() {
    return StringUtils.hasText(alias) ? String.format("%s as %s", name, alias) : name;
  }

  public Condition equal(T value) {
    return new Condition(name, Condition.Operator.equal, value);
  }

  public Condition equal(Column<?> column) {
    return new Condition(name, Condition.Operator.equal, column);
  }

  public Condition notEqual(T value) {
    return new Condition(name, Condition.Operator.notEqual, value);
  }

  public Condition notEqual(Column<?> column) {
    return new Condition(name, Condition.Operator.notEqual, column);
  }

  public Condition greaterThan(T value) {
    return new Condition(name, Condition.Operator.greaterThan, value);
  }

  public Condition greaterThan(Column<?> column) {
    return new Condition(name, Condition.Operator.greaterThan, column);
  }

  public Condition greaterThanOrEqual(T value) {
    return new Condition(name, Condition.Operator.greaterOrEqual, value);
  }

  public Condition greaterThanOrEqual(Column<?> column) {
    return new Condition(name, Condition.Operator.greaterOrEqual, column);
  }

  public Condition lessThan(T value) {
    return new Condition(name, Condition.Operator.lessThan, value);
  }

  public Condition lessThan(Column<?> column) {
    return new Condition(name, Condition.Operator.lessThan, column);
  }

  public Condition lessThanOrEqual(T value) {
    return new Condition(name, Condition.Operator.lessOrEqual, value);
  }

  public Condition lessThanOrEqual(Column<?> column) {
    return new Condition(name, Condition.Operator.lessOrEqual, column);
  }

  public Condition like(String pattern) {
    return new Condition(name, Condition.Operator.like, pattern);
  }

  public Condition in(Collection<T> values) {
    return new Condition(name, Condition.Operator.in, values);
  }

  public Condition notIn(Collection<T> values) {
    return new Condition(name, Condition.Operator.notIn, values);
  }

  public Condition isNull() {
    return new Condition(name, Condition.Operator.isNull);
  }

  public Condition isNotNull() {
    return new Condition(name, Condition.Operator.isNotNull);
  }
}

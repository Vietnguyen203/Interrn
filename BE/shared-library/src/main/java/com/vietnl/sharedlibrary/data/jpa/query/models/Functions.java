package com.vietnl.sharedlibrary.data.jpa.query.models;

import com.eps.shared.data.jpa.query.interfaces.ImmutableColumn;
import jakarta.persistence.criteria.JoinType;

public class Functions {

  public static Table table(String name) {
    return new Table(name);
  }

  public static Table table(Class<?> entityClass) {
    return new Table(entityClass.getSimpleName());
  }

  public static <T> Column<T> column(String name) {
    return new Column<>(name);
  }

  public static JoinExpression join(final JoinType type, final String tableName) {

    return new JoinExpression(type, tableName);
  }

  // Aggregate functions
  public static <T> Column<T> count(String name) {
    return column(String.format("count(%s)", name));
  }

  public static <T> Column<T> count(ImmutableColumn column) {

    return count(column.getName());
  }

  // Aggregate functions
  public static <T> Column<T> sum(String name) {
    return column(String.format("sum(%s)", name));
  }

  public static <T> Column<T> sum(ImmutableColumn column) {
    return sum(column.getName());
  }

  public static <T> Column<T> avg(String name) {
    return column(String.format("avg(%s)", name));
  }

  public static <T> Column<T> avg(ImmutableColumn column) {
    return avg(column.getName());
  }

  public static <T> Column<T> min(String name) {
    return column(String.format("min(%s)", name));
  }

  public static <T> Column<T> min(ImmutableColumn column) {
    return min(column.getName());
  }

  public static <T> Column<T> max(String name) {
    return column(String.format("max(%s)", name));
  }

  public static <T> Column<T> max(ImmutableColumn column) {
    return max(column.getName());
  }
}

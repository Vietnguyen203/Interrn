package com.vietnl.sharedlibrary.data.jpa.query.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
public class Condition {

  private String sql;
  private final List<Param> params = new ArrayList<>();

  protected Condition(String name, Operator operator, Column<?> column) {
    sql = name + String.format(operator.format, column.getName());
  }

  protected Condition(String name, Operator operator, Object value) {
    String key = safeParamName(name);

    if (value instanceof List<?>) {
      String placeholder =
          IntStream.range(0, ((List<?>) value).size())
              .mapToObj(
                  i -> {
                    String newKey = key + "_" + i;
                    params.add(new Param(newKey, ((List<?>) value).get(i)));
                    return ":" + newKey;
                  })
              .collect(Collectors.joining(","));
      sql = name + String.format(operator.format, placeholder);
    } else {
      sql = name + String.format(operator.format, ":" + key);
      params.add(new Param(key, value));
    }
  }

  protected Condition(String name, Operator operator) {
    String key = safeParamName(name);
    sql = String.format(operator.format, key);
  }

  public Condition and(Condition other) {
    sql = "(" + sql + " and " + other.sql + ")";
    params.addAll(other.params);

    return this;
  }

  public Condition or(Condition other) {
    sql = "(" + sql + " or " + other.sql + ")";
    params.addAll(other.params);
    return this;
  }

  public String getSql() {

    return String.format(" (%s) ", sql);
  }

  @Data
  @AllArgsConstructor
  public static class Param {
    private String key;
    private Object value;
  }

  public enum Operator {
    equal(" = %s"),
    notEqual(" <> %s"),
    greaterThan(" > %s"),
    lessThan(" < %s"),
    greaterOrEqual(" >= %s"),
    lessOrEqual(" <= %s"),
    like(" LIKE ?"),
    in(" IN (%s)"),
    notIn(" NOT IN (%s)"),
    isNull(" IS NULL"),
    isNotNull(" IS NOT NULL");

    @Getter private final String format;

    Operator(String format) {
      this.format = format;
    }
  }

  public String safeParamName(String name) {
    // Dùng khi cần bind giá trị
    return name.replaceAll("[^a-zA-Z0-9]", "_");
  }
}

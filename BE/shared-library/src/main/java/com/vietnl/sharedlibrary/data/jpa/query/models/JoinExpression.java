package com.vietnl.sharedlibrary.data.jpa.query.models;

import jakarta.persistence.criteria.JoinType;
import lombok.Getter;

@Getter
public class JoinExpression {

  private final JoinType type;
  private final String tableName;
  private Condition condition;

  protected JoinExpression(JoinType type, String tableName) {
    this.tableName = tableName;
    this.type = type;
  }

  public JoinExpression on(final Condition condition) {
    this.condition = condition;
    return this;
  }

  public String getSql() {

    return String.format(" %s join %s ON %s ", type, tableName, condition.getSql());
  }
}

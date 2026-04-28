package com.vietnl.sharedlibrary.data.jpa.query.interfaces;

import com.eps.shared.data.jpa.query.models.Column;
import com.eps.shared.data.jpa.query.models.Functions;
import com.eps.shared.data.jpa.query.models.JoinExpression;
import jakarta.persistence.criteria.JoinType;

public interface ImmutableTable extends Named {

  String getTableName();

  default <T> Column<T> column(String name) {
    return Functions.column(String.format("%s.%s", getTableName(), name));
  }

  default JoinExpression join(final JoinType type) {

    return Functions.join(type, getName());
  }

  default JoinExpression leftJoin() {
    return Functions.join(JoinType.LEFT, getName());
  }

  default JoinExpression rightJoin() {
    return Functions.join(JoinType.RIGHT, getName());
  }

  default JoinExpression innerJoin() {

    return Functions.join(JoinType.INNER, getName());
  }
}

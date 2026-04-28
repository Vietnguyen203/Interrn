package com.vietnl.sharedlibrary.data.jpa.query.models;

import com.eps.shared.core.exception.CommonErrorMessage;
import com.eps.shared.core.exception.ResponseException;
import com.eps.shared.data.jpa.query.JpaFactory;
import com.eps.shared.data.jpa.query.interfaces.ImmutableColumn;
import com.eps.shared.data.jpa.query.interfaces.ImmutableTable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ToString
public class QueryCommand {

  private final List<ImmutableColumn> selectedColumns = new ArrayList<>();
  private ImmutableTable table;
  @Getter private final List<Condition> whereConditions = new ArrayList<>();
  private final List<ImmutableColumn> groupedColumns = new ArrayList<>();
  private final List<JoinExpression> joinExpressions = new ArrayList<>();
  private final List<Condition> havingConditions = new ArrayList<>();
  @Setter @Getter private boolean isNative = false;

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Builder() {}

    private final QueryCommand command = new QueryCommand();

    public Builder select(final ImmutableColumn... columns) {

      command.selectedColumns.addAll(List.of(columns));
      return this;
    }

    public Builder from(final ImmutableTable table) {
      command.table = table;
      return this;
    }

    public Builder join(JoinExpression expression) {
      command.joinExpressions.add(expression);
      return this;
    }

    public Builder where(final Condition... conditions) {

      command.whereConditions.addAll(List.of(conditions));
      return this;
    }

    public Builder having(final Condition... conditions) {

      command.havingConditions.addAll(List.of(conditions));
      return this;
    }

    public Builder group(final ImmutableColumn... columns) {
      command.groupedColumns.addAll(List.of(columns));
      return this;
    }

    public QueryCommand build() {
      return command;
    }
  }

  public Stream getResultStream() {
    return getResultStream(null);
  }

  public Stream getResultStream(Pageable pageable) {
    EntityManager entityManager = JpaFactory.getEntityManager();
    if (entityManager == null) {
      throw new RuntimeException(CommonErrorMessage.DATABASE_NOT_CONNECTED.val());
    }

    String sql = toSql();

    if (pageable != null && !pageable.getSort().isEmpty()) {
      StringBuilder orderBy = new StringBuilder(" ORDER BY ");

      List<String> orders = new ArrayList<>();

      for (Sort.Order order : pageable.getSort()) {
        orders.add(order.getProperty() + " " + order.getDirection().name());
      }
      orderBy.append(String.join(", ", orders));
      sql += orderBy;
    }

    Query query;
    if (isNative) {
      query = entityManager.createNativeQuery(sql, Tuple.class);
    } else {
      query = JpaFactory.getEntityManager().createQuery(sql, Tuple.class);
    }

    for (Condition condition : getWhereConditions()) {
      for (Condition.Param param : condition.getParams()) {
        query.setParameter(param.getKey(), param.getValue());
      }
    }

    if (pageable != null) {
      int offset = (int) pageable.getOffset(); // vị trí bắt đầu
      int limit = pageable.getPageSize(); // số lượng bản ghi tối đa
      query.setFirstResult(offset);
      query.setMaxResults(limit);
    }

    return query.getResultStream();
  }

  public String toSql() {
    String SPACE_SEPARATOR = " ";
    // From
    if (table == null) {
      throw new ResponseException(
          HttpStatus.BAD_REQUEST, CommonErrorMessage.FROM_TABLE_IS_REQUIRED);
    }

    StringBuilder sql = new StringBuilder();
    // Select
    sql.append("SELECT ");
    if (selectedColumns.isEmpty()) {
      if (isNative) {
        sql.append("*");
      } else {
        sql.append(table.getTableName());
      }
    } else {
      sql.append("  ")
          .append(
              selectedColumns.stream()
                  .map(ImmutableColumn::getName)
                  .collect(Collectors.joining(", ")));
    }
    sql.append(SPACE_SEPARATOR);

    sql.append(" FROM ").append(table.getName());
    sql.append(SPACE_SEPARATOR);

    // WHERE
    if (!joinExpressions.isEmpty()) {
      sql.append(
          joinExpressions.stream().map(JoinExpression::getSql).collect(Collectors.joining(" \n")));
    }
    sql.append(SPACE_SEPARATOR);

    // WHERE
    if (!whereConditions.isEmpty()) {
      sql.append(" WHERE ")
          .append(
              whereConditions.stream().map(Condition::getSql).collect(Collectors.joining(" AND ")));
    }
    sql.append(SPACE_SEPARATOR);

    // GROUP BY
    if (!groupedColumns.isEmpty()) {
      sql.append(" GROUP BY ")
          .append(
              groupedColumns.stream()
                  .map(ImmutableColumn::getName)
                  .collect(Collectors.joining(", ")));
    }
    sql.append(SPACE_SEPARATOR);

    // HAVING
    if (!havingConditions.isEmpty()) {
      sql.append(" HAVING ")
          .append(
              havingConditions.stream()
                  .map(Condition::getSql)
                  .collect(Collectors.joining(" AND ")));
    }
    sql.append(SPACE_SEPARATOR);

    return sql.toString();
  }
}

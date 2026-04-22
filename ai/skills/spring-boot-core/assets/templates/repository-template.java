package ${PACKAGE}.infrastructure.persistence.repositories;

import static com.eps.shared.services.query.models.Functions.table;

import com.eps.shared.interfaces.repository.IBaseRepository;
import com.eps.shared.services.query.interfaces.ImmutableTable;
import com.eps.shared.services.query.models.Condition;
import com.eps.shared.services.query.models.QueryCommand;
import ${PACKAGE}.domain.models.entities.${ENTITY};
import ${PACKAGE}.domain.models.enums.${ENTITY}Status;
import ${PACKAGE}.domain.models.values.QueryRequest;
import jakarta.persistence.Tuple;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Repository;

/**
 * Repository implementation for ${ENTITY}.
 *
 * <p>
 * This interface extends:
 * - IBaseRepository: provides JpaRepository + JpaSpecificationExecutor
 * - ${ENTITY}Persistence: domain persistence contract
 *
 * <p>
 * Spring Data JPA auto-implements standard methods based on naming convention.
 * Custom queries use default methods with QueryCommand builder.
 */
@Repository
public interface $ {
  ENTITY}Repository extends IBaseRepository<${ENTITY},UUID>,${ENTITY}Persistence{

  // ==================== Spring Data JPA auto-implemented ====================
  // findByOrgId, findByCode, existsByCode... are auto-implemented by method
  // naming

  /**
   * Case-insensitive existence check (delegated).
   */
  @Override
  default boolean existsByCode(String code) {
    return existsByCodeIgnoreCase(code.trim());
  }

  boolean existsByCodeIgnoreCase(String code);

  /**
   * Case-insensitive find by code.
   */
  @Override default Optional<${ENTITY}>

  findByCode(String code) {
    return findByCodeIgnoreCase(code.trim());
  }

  Optional<${ENTITY}>

  findByCodeIgnoreCase(String code);

  // ==================== Dynamic Query with QueryCommand ====================

  /**
   * Execute dynamic query using QueryCommand builder.
   *
   * <p>
   * Pattern:
   * <ol>
   * <li>Create ImmutableTable with alias</li>
   * <li>Build QueryCommand with select, from, where</li>
   * <li>Convert raw values to typed values (String → UUID)</li>
   * <li>Build Condition using Operator enum</li>
   * <li>Execute and map results from Tuple</li>
   * </ol>
   */
  @Override default List<UUID>query${ENTITY}(
  QueryRequest queryRequest)
  {
    ImmutableTable entityTable = table(${ENTITY}.class).as("e");

    QueryCommand.Builder builder = QueryCommand.builder()
        .select(entityTable.column("id"))
        .from(entityTable);

    // Type conversion: String/Collection<String> → UUID/Collection<UUID>
    Object rawValue = queryRequest.getValue();
    Object typedValue = convertToTypedValue(rawValue);

    // Build condition from Operator enum
    Condition condition = queryRequest.getOperator()
        .build(entityTable.column(queryRequest.getField()), typedValue);

    builder.where(condition);

    // Execute query and map to UUID list
    return ((Stream<Object>) builder.build().getResultStream())
        .map(o -> ((Tuple) o).get(0, UUID.class))
        .collect(Collectors.toList());
  }

  /**
   * Convert raw query values to properly typed values.
   * Handles String → UUID conversion for both single values and collections.
   */
  private static Object convertToTypedValue(Object rawValue) {
    if (rawValue instanceof Collection<?> collection) {
      if (!collection.isEmpty() && collection.iterator().next() instanceof String) {
        return collection.stream()
            .map(s -> UUID.fromString((String) s))
            .toList();
      }
      return collection;
    } else if (rawValue instanceof String str) {
      return UUID.fromString(str);
    }
    return rawValue;
  }

  // ==================== Example: Query with JOIN ====================
  /*
   * default List<SubData> get${ENTITY}WithRelations(UUID parentId) {
   * ImmutableTable mainTable = table(${ENTITY}.class).as("e");
   * ImmutableTable relTable = table(Rel${ENTITY}Parent.class).as("rel");
   * 
   * QueryCommand query = QueryCommand.builder()
   * .select(
   * mainTable.column("id"),
   * mainTable.column("code"),
   * mainTable.column("name")
   * )
   * .from(mainTable)
   * .join(relTable.innerJoin().on(
   * mainTable.column("id").equal(relTable.column("entityId"))
   * ))
   * .where(relTable.column("parentId").equal(parentId))
   * .build();
   * 
   * return ((Stream<Object>) query.getResultStream())
   * .map(o -> {
   * Tuple tuple = (Tuple) o;
   * return SubData.builder()
   * .id(tuple.get(0, UUID.class))
   * .code(tuple.get(1, String.class))
   * .name(tuple.get(2, String.class))
   * .build();
   * })
   * .collect(Collectors.toList());
   * }
   */

  // ==================== Example: Aggregate Query ====================
  /*
   * default Map<UUID, Long> countByOrg() {
   * ImmutableTable entityTable = table(${ENTITY}.class).as("e");
   * 
   * QueryCommand query = QueryCommand.builder()
   * .select(
   * entityTable.column("orgId"),
   * Functions.count(entityTable.column("id")).as("total")
   * )
   * .from(entityTable)
   * .group(entityTable.column("orgId"))
   * .build();
   * 
   * return ((Stream<Object>) query.getResultStream())
   * .collect(Collectors.toMap(
   * o -> ((Tuple) o).get(0, UUID.class),
   * o -> ((Tuple) o).get(1, Long.class)
   * ));
   * }
   */
}
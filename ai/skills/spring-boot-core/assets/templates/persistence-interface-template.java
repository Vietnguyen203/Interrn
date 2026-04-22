package ${PACKAGE}.domain.services.persistence;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import ${PACKAGE}.domain.models.entities.${ENTITY};
import ${PACKAGE}.domain.models.enums.${ENTITY}Status;
import ${PACKAGE}.domain.models.values.QueryRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain persistence interface for ${ENTITY}.
 *
 * <p>
 * This interface defines the contract for ${ENTITY} persistence operations.
 * It extends IBasePersistence which provides:
 * - ICrudPersistence: save, findById, findByIdOrNull, existsById, deleteById,
 * deleteAll...
 * - IJpaGetAllPersistence: findAll(Specification, Pageable)
 *
 * <p>
 * Implementation: Infrastructure layer repository (${ENTITY}Repository)
 */
public interface $ {
  ENTITY}Persistence extends IBasePersistence<${ENTITY},UUID>{

  // ==================== Standard CRUD (inherited from IBasePersistence)
  // ====================
  // E save(E entity);
  // <T extends E> List<T> saveAll(Iterable<T> entities);
  // Optional<E> findById(ID id);
  // E findByIdOrNull(ID id);
  // boolean existsById(ID id);
  // List<E> findAllByIdIn(Iterable<ID> ids);
  // void deleteById(ID id);
  // void delete(E entity);
  // void deleteAll(Iterable<? extends E> entities);
  // Page<E> findAll(@Nullable Specification<E> spec, Pageable pageable);

  // ==================== Domain-specific queries ====================

  /**
   * Find all entities (without pagination).
   */
  List<${ENTITY}>findAll();

  /**
   * Find entities by organization ID.
   */
  List<${ENTITY}>
  findByOrgId(UUID orgId);

  /**
   * Find entities by status and date condition.
   * Useful for scheduled jobs (e.g., unlock users, expire records).
   */
  List<${ENTITY}>

  findByUpdatedAtBeforeAndStatusNot(LocalDateTime dateTime, ${ENTITY}Status status);

  // ==================== Existence checks ====================

  /**
   * Check if entity exists by code (case-insensitive).
   */
  boolean existsByCode(String code);

  /**
   * Find entity by code.
   */
  Optional<${ENTITY}>

  findByCode(String code);

  // ==================== Dynamic queries (QueryCommand) ====================

  /**
   * Execute dynamic query and return matching entity IDs.
   *
   * <p>Implementation uses QueryCommand builder from shared-library:
   * <pre>
   * ImmutableTable table = Functions.table(${ENTITY}.class).as("e");
   * QueryCommand.builder()
   *     .select(table.column("id"))
   *     .from(table)
   *     .where(queryRequest.getOperator().build(table.column("field"), typedValue))
   *     .build()
   *     .getResultStream();
   * </pre>
   *
   * @param queryRequest contains field, operator, and value for filtering
   * @return list of matching entity IDs
   */
  List<UUID> query${ENTITY}(QueryRequest queryRequest);
}
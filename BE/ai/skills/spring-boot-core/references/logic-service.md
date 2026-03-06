# Logic Service (Use Case) Convention

## Mục tiêu
Tài liệu này mô tả **chuẩn service layer** dựa trên shared-library interfaces tại `com.eps.shared.interfaces.services`.

---

## 🏛️ Kiến trúc tổng quan

```
┌─────────────────────────────────────────────────────────────────┐
│                         IBaseService                            │
│       <E, ID, RES, REQ, PAGE_RES>                               │
│  ┌────────────────────────┬────────────────────────┐            │
│  │   extends ICrudService │  extends IGetAllService│            │
│  └────────────────────────┴────────────────────────┘            │
│       ▲ Bắt buộc: getPersistence()                              │
└─────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌───────────────┐   ┌─────────────────┐   ┌─────────────────┐
│  ICrudService │   │ IGetAllService  │   │ IResponseMapper │
│  (C, R, U, D) │   │   (Page/Search) │   │ (Entity → DTO)  │
└───────────────┘   └─────────────────┘   └─────────────────┘
        │
        ├── ICreateService
        ├── IUpdateService
        ├── IPatchService
        ├── IGetService
        └── IDeleteService
```

---

## 📦 Interface Hierarchy

### `IBaseService<E, ID, RES, REQ, PAGE_RES>`

```java
public interface IBaseService<E, ID, RES, REQ, PAGE_RES>
    extends ICrudService<E, ID, RES, REQ>, IGetAllService<E, PAGE_RES> {

  // ⭐ BẮT BUỘC OVERRIDE
  IBasePersistence<E, ID> getPersistence();

  // Default implementations
  default ICrudPersistence<E, ID> getCrudPersistence() {
    return getPersistence();
  }

  default IJpaGetAllPersistence<E> getJpaGetAllPersistence() {
    return getPersistence();
  }
}
```

### `ICrudService<E, ID, RES, REQ>`

```java
public interface ICrudService<E, ID, RES, REQ>
    extends ICreateService<E, ID, RES, REQ>,
            IUpdateService<E, ID, RES, REQ>,
            IPatchService<E, ID, RES, REQ>,
            IGetService<E, ID, RES>,
            IDeleteService<E, ID> {}
```

---

## 🔨 ICreateService - Chi tiết Method Signatures

```java
public interface ICreateService<E, ID, RES, REQ>
    extends IResponseMapper<E, RES>, ICrudPersistenceProvider<E, ID> {

  // ==================== MAIN METHOD ====================
  @Transactional
  default RES create(
      HeaderContext context,
      REQ request,
      TriConsumer<HeaderContext, E, REQ> validationCreateHandler,
      TriConsumer<HeaderContext, E, REQ> mappingAuditingEntityHandler,
      TriConsumer<HeaderContext, E, REQ> mappingEntityHandler,
      TriConsumer<HeaderContext, E, REQ> postHandler,
      BiFunction<HeaderContext, E, RES> mappingResponseHandler
  );

  // ==================== SIMPLIFIED METHOD ====================
  @Transactional
  default RES create(HeaderContext context, REQ request);

  // ==================== OVERRIDE HOOKS ====================

  /**
   * Validate request trước khi tạo entity.
   * Override để thêm business validation.
   */
  default void validateCreateRequest(
      HeaderContext context,  // Chứa userCode, locale, orgId...
      E entity,               // Entity mới (chưa có data)
      REQ request             // Request từ client
  ) {}

  /**
   * Mapping auditing fields (id, createdBy, updatedBy).
   * Thường không cần override.
   */
  default void mappingCreateAuditingEntity(
      HeaderContext context,
      E entity,
      REQ request
  ) {
    // Auto-generate UUID và set createdBy/updatedBy
    if (GenericTypeUtils.getFieldValue(entity, "id") == null) {
      GenericTypeUtils.updateData(entity, "id", UuidCreator.getTimeOrderedEpoch());
    }
    GenericTypeUtils.updateData(entity, "createdBy", context.getUserCode());
    GenericTypeUtils.updateData(entity, "updatedBy", context.getUserCode());
  }

  /**
   * Mapping data từ request → entity.
   * Override để customize logic (normalize, relations...).
   */
  default void mappingCreateEntity(
      HeaderContext context,
      E entity,
      REQ request
  ) {
    FnCommon.copyProperties(entity, request);
  }

  /**
   * Post-processing sau khi save (call external service, send notification...).
   */
  default void postCreateHandler(
      HeaderContext context,
      E entity,               // Entity đã được save (có ID)
      REQ request
  ) {}
}
```

---

## 🔄 IUpdateService - Chi tiết Method Signatures

```java
public interface IUpdateService<E, ID, RES, REQ>
    extends IResponseMapper<E, RES>, ICrudPersistenceProvider<E, ID> {

  // ==================== MAIN METHOD ====================
  @Transactional
  default RES update(
      HeaderContext context,
      ID id,
      REQ request,
      QuadConsumer<HeaderContext, ID, E, REQ> validationHandler,
      TriConsumer<HeaderContext, E, REQ> mappingUpdateAuditingEntity,
      TriConsumer<HeaderContext, E, REQ> mappingHandler,
      PentaConsumer<HeaderContext, E, E, ID, REQ> postHandler,
      BiFunction<HeaderContext, E, RES> mappingResponseHandler
  );

  // ==================== SIMPLIFIED METHOD ====================
  @Transactional
  default RES update(HeaderContext context, ID id, REQ request);

  // ==================== OVERRIDE HOOKS ====================

  /**
   * Validate request trước khi update.
   * @param id ID của entity đang update
   * @param entity Entity hiện tại từ DB
   */
  default void validateUpdateRequest(
      HeaderContext context,
      ID id,
      E entity,               // Entity hiện tại (before update)
      REQ request
  ) {}

  /**
   * Mapping auditing fields.
   */
  default void mappingUpdateAuditingEntity(
      HeaderContext context,
      E entity,
      REQ request
  ) {
    GenericTypeUtils.updateData(entity, "updatedBy", context.getUserCode());
  }

  /**
   * Mapping data từ request → entity.
   */
  default void mappingUpdateEntity(
      HeaderContext context,
      E entity,
      REQ request
  ) {
    FnCommon.copyProperties(entity, request);
  }

  /**
   * Post-processing sau khi update.
   * @param originalEntity Bản sao entity TRƯỚC KHI update (để so sánh changes)
   * @param entity Entity SAU KHI update
   */
  default void postUpdateHandler(
      HeaderContext context,
      E originalEntity,       // Entity trước khi update (deep copy)
      E entity,               // Entity sau khi update
      ID id,
      REQ request
  ) {}
}
```

---

## 🩹 IPatchService - Chi tiết Method Signatures

```java
public interface IPatchService<E, ID, RES, REQ>
    extends IResponseMapper<E, RES>, ICrudPersistenceProvider<E, ID> {

  // ==================== MAIN METHOD ====================
  @Transactional
  default RES patch(
      HeaderContext context,
      ID id,
      REQ request,
      QuadConsumer<HeaderContext, ID, E, REQ> validationHandler,
      TriConsumer<HeaderContext, E, REQ> mappingPatchAuditingEntity,
      TriConsumer<HeaderContext, E, REQ> mappingHandler,
      PentaConsumer<HeaderContext, E, E, ID, REQ> postHandler,
      BiFunction<HeaderContext, E, RES> mappingResponseHandler
  );

  // ==================== SIMPLIFIED METHOD ====================
  @Transactional
  default RES patch(HeaderContext context, ID id, REQ request);

  // ==================== OVERRIDE HOOKS ====================

  default void validatePatchRequest(
      HeaderContext context,
      ID id,
      E entity,
      REQ request
  ) {}

  default void mappingPatchAuditingEntity(
      HeaderContext context,
      E entity,
      REQ request
  ) {
    GenericTypeUtils.updateData(entity, "updatedBy", context.getUserCode());
  }

  /**
   * ⭐ Khác với Update: chỉ copy NOT NULL properties
   */
  default void mappingPatchEntity(
      HeaderContext context,
      E entity,
      REQ request
  ) {
    FnCommon.copyNotNullProperties(entity, request); // Chỉ copy field không null
  }

  default void postPatchHandler(
      HeaderContext context,
      E originalEntity,
      E entity,
      ID id,
      REQ request
  ) {}
}
```

---

## 🗑️ IDeleteService - Chi tiết Method Signatures

```java
public interface IDeleteService<E, ID> extends ICrudPersistenceProvider<E, ID> {

  // ==================== SINGLE DELETE ====================
  @Transactional
  default void delete(
      HeaderContext context,
      ID id,
      TriConsumer<HeaderContext, ID, E> validationHandler,
      TriConsumer<HeaderContext, ID, E> postDeleteHandler
  );

  @Transactional
  default void delete(HeaderContext context, ID id);

  // ==================== OVERRIDE HOOKS ====================

  /**
   * Validate trước khi xóa (check relations, permissions...).
   */
  default void validateDelete(
      HeaderContext context,
      ID id,
      E entity                // Entity sắp bị xóa
  ) {}

  /**
   * Post-processing sau khi xóa (cleanup, audit log...).
   */
  default void postDeleteHandler(
      HeaderContext context,
      ID id,
      E entity                // Entity đã bị xóa
  ) {}

  // ==================== BATCH DELETE ====================
  @Transactional
  default void deleteBatch(
      HeaderContext context,
      List<ID> ids,
      TriConsumer<HeaderContext, List<ID>, List<E>> validationHandler,
      TriConsumer<HeaderContext, List<ID>, List<E>> postDeleteHandler
  );

  @Transactional
  default void deleteBatch(HeaderContext context, List<ID> ids);

  default void validateDeleteBatch(
      HeaderContext context,
      List<ID> ids,
      List<E> entities
  ) {}

  default void postDeleteBatchHandler(
      HeaderContext context,
      List<ID> ids,
      List<E> entities
  ) {}
}
```

---

## 🔍 IGetService - Chi tiết Method Signatures

```java
public interface IGetService<E, ID, RES>
    extends IResponseMapper<E, RES>, ICrudPersistenceProvider<E, ID> {

  /**
   * Tìm entity theo ID, throw 404 nếu không tìm thấy.
   */
  default RES getById(
      HeaderContext context,
      ID id
  ) {
    E entity = getCrudPersistence().findByIdOrNull(id);
    if (entity == null) {
      throw new ResponseException(HttpStatus.NOT_FOUND, CommonErrorMessage.OBJECT_NOT_FOUND);
    }
    return mappingResponse(context, entity);
  }
}
```

---

## 📄 IGetAllService - Chi tiết Method Signatures

```java
public interface IGetAllService<E, RES> {

  IJpaGetAllPersistence<E> getJpaGetAllPersistence();

  // ==================== MAIN METHOD ====================
  default Page<RES> getAll(
      HeaderContext context,
      String search,                              // Từ khóa tìm kiếm (LIKE)
      Integer page,                               // Số trang (0-based)
      Integer pageSize,                           // Số record/trang
      String sort,                                // "field,asc" hoặc "field,desc"
      Map<String, Object> filter,                 // Dynamic filters
      BiFunction<HeaderContext, Page<E>, Page<RES>> mappingPageResponseHandler
  );

  default Page<RES> getAll(
      HeaderContext context,
      String search,
      Integer page,
      Integer pageSize,
      String sort,
      Map<String, Object> filter
  );

  // ==================== OVERRIDE HOOKS ====================

  /**
   * Mapping Page<Entity> → Page<DTO>.
   * Override nếu cần mapping phức tạp.
   */
  default Page<RES> mappingPageResponse(
      HeaderContext context,
      Page<E> items
  ) {
    return items.map(item -> {
      RES resItem = GenericTypeUtils.getNewInstance(this, IGetAllService.class, PositionType.LAST);
      FnCommon.copyProperties(resItem, item);
      return resItem;
    });
  }

  /**
   * ⭐ Override để xác định fields searchable.
   * Mặc định: ["name", "ten", "ma", "code"]
   */
  default String[] getSearchFieldNames() {
    return new String[] {"name", "ten", "ma", "code"};
  }

  /**
   * Build custom filter predicates.
   * Override để thêm filter logic đặc biệt.
   */
  default List<Predicate> buildFilterQuery(
      HeaderContext context,
      Root<E> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      Map<String, Object> filter
  ) {
    return new ArrayList<>();
  }

  // ==================== AUTO FILTER (Entity fields) ====================

  /**
   * Auto-build predicates từ entity attributes.
   * Supports: UUID, Enum, DateTime (với _from/_to prefix), primitives.
   */
  default List<Predicate> buildEntityQuery(
      Set<Attribute<? super E, ?>> fieldNames,
      Root<E> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      Map<String, Object> filter
  );

  /**
   * Build search predicate (LIKE on searchable fields).
   */
  default Predicate buildSearchQuery(
      Set<Attribute<? super E, ?>> fieldNames,
      Root<E> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      String search
  );
}
```

### Supported Filter Types

| Type | Example Filter | Auto Conversion |
|------|---------------|-----------------|
| UUID | `{"userId": "550e8400-..."}` | String → UUID |
| Enum | `{"status": "ACTIVE"}` | String → Enum |
| DateTime | `{"createdAt_from": "2024-01-01", "createdAt_to": "2024-12-31"}` | Range query |
| String | `{"name": "John"}` | Equal |
| Integer/Long | `{"age": 25}` | Equal |
| Boolean | `{"active": true}` | Equal |

---

## 🎭 IResponseMapper - Chi tiết

```java
public interface IResponseMapper<E, RES> {

  /**
   * Mapping Entity → Response DTO.
   * Override để customize (nested objects, computed fields...).
   */
  default RES mappingResponse(
      HeaderContext context,
      E entity
  ) {
    RES res = GenericTypeUtils.getNewInstance(this, IResponseMapper.class, PositionType.LAST);
    FnCommon.copyProperties(res, entity);
    return res;
  }
}
```

---

## 📝 Service Implementation Template

```java
@Service
@RequiredArgsConstructor
public class UserAccountService
    implements IBaseService<UserAccount, UUID, UserAccountResponse, UserAccountRequest, UserAccountPageResponse> {

  private final UserAccountPersistence persistence;
  private final UserAccountValidator validator;
  private final KeycloakCommunication keycloakCommunication;

  // ⭐ BẮT BUỘC
  @Override
  public IBasePersistence<UserAccount, UUID> getPersistence() {
    return persistence;
  }

  // ==================== CREATE HOOKS ====================

  @Override
  public void validateCreateRequest(HeaderContext context, UserAccount entity, UserAccountRequest request) {
    validator.validateCreate(request);
  }

  @Override
  public void mappingCreateEntity(HeaderContext context, UserAccount entity, UserAccountRequest request) {
    FnCommon.copyProperties(entity, request);
    entity.setUserCode(normalizeUserCode(request.getUserCode()));
    entity.setStatus(UserStatus.ACTIVE);
  }

  @Override
  public void postCreateHandler(HeaderContext context, UserAccount entity, UserAccountRequest request) {
    keycloakCommunication.createUser(entity);
  }

  // ==================== UPDATE HOOKS ====================

  @Override
  public void validateUpdateRequest(HeaderContext context, UUID id, UserAccount entity, UserAccountRequest request) {
    validator.validateUpdate(id, request);
  }

  @Override
  public void postUpdateHandler(HeaderContext context, UserAccount originalEntity, UserAccount entity, UUID id, UserAccountRequest request) {
    if (!originalEntity.getEmail().equals(entity.getEmail())) {
      keycloakCommunication.updateEmail(entity);
    }
  }

  // ==================== DELETE HOOKS ====================

  @Override
  public void validateDelete(HeaderContext context, UUID id, UserAccount entity) {
    if (entity.getStatus() == UserStatus.SYSTEM) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Cannot delete system user");
    }
  }

  @Override
  public void postDeleteHandler(HeaderContext context, UUID id, UserAccount entity) {
    keycloakCommunication.deleteUser(entity.getKeycloakId());
  }

  // ==================== GET ALL HOOKS ====================

  @Override
  public String[] getSearchFieldNames() {
    return new String[] {"userCode", "email", "fullName"};
  }

  @Override
  public List<Predicate> buildFilterQuery(HeaderContext context, Root<UserAccount> root, CriteriaQuery<?> query, CriteriaBuilder cb, Map<String, Object> filter) {
    List<Predicate> predicates = new ArrayList<>();

    if (filter.containsKey("groupId")) {
      // Custom join query
      UUID groupId = UUID.fromString((String) filter.get("groupId"));
      predicates.add(/* custom predicate */);
    }

    return predicates;
  }

  // ==================== RESPONSE MAPPING ====================

  @Override
  public UserAccountResponse mappingResponse(HeaderContext context, UserAccount entity) {
    UserAccountResponse response = new UserAccountResponse();
    FnCommon.copyProperties(response, entity);
    response.setGroupNames(getGroupNames(entity.getId()));
    return response;
  }
}
```

---

## 📋 Quick Reference

| Operation | Interface | Key Override Methods |
|-----------|-----------|---------------------|
| **CREATE** | `ICreateService` | `validateCreateRequest`, `mappingCreateEntity`, `postCreateHandler` |
| **UPDATE** | `IUpdateService` | `validateUpdateRequest`, `mappingUpdateEntity`, `postUpdateHandler` |
| **PATCH** | `IPatchService` | `validatePatchRequest`, `mappingPatchEntity`, `postPatchHandler` |
| **DELETE** | `IDeleteService` | `validateDelete`, `postDeleteHandler` |
| **GET BY ID** | `IGetService` | `mappingResponse` |
| **GET ALL** | `IGetAllService` | `getSearchFieldNames`, `buildFilterQuery`, `mappingPageResponse` |

---

## 📁 File Locations

| Interface | Package |
|-----------|---------|
| IBaseService | `com.eps.shared.interfaces.services` |
| ICrudService | `com.eps.shared.interfaces.services` |
| ICreateService | `com.eps.shared.interfaces.services` |
| IUpdateService | `com.eps.shared.interfaces.services` |
| IPatchService | `com.eps.shared.interfaces.services` |
| IDeleteService | `com.eps.shared.interfaces.services` |
| IGetService | `com.eps.shared.interfaces.services` |
| IGetAllService | `com.eps.shared.interfaces.services` |
| IResponseMapper | `com.eps.shared.interfaces.services` |

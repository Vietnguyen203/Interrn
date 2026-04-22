# Database Call (Persistence + Repository) Convention

## Mục tiêu
Tài liệu này mô tả **chuẩn truy cập DB** trong project, dựa trên phân tích chi tiết source code trong `backend/shared-library/src/main/java/com/eps/shared`.

---

## 🏛️ Kiến trúc tổng quan

```
┌─────────────────────────────────────────────────────────────────┐
│                       DOMAIN LAYER                              │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │           Domain Persistence Interface                   │   │
│  │         (extends IBasePersistence<E, ID>)                │   │
│  │   Ví dụ: UserAccountPersistence, UserGroupPersistence   │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ▲
                              │ implements
┌─────────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                         │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              @Repository Interface                       │   │
│  │    (extends IBaseRepository<E, ID> + ...Persistence)     │   │
│  │   Ví dụ: NguoiDungRepository, UserGroupRepository        │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ▲
                              │ extends
┌─────────────────────────────────────────────────────────────────┐
│                      SHARED LIBRARY                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  IBaseRepository<E, ID>                                   │  │
│  │    extends JpaRepository<E, ID>                           │  │
│  │            + JpaSpecificationExecutor<E>                  │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  IBasePersistence<E, ID>                                  │  │
│  │    extends ICrudPersistence<E, ID>                        │  │
│  │            + IJpaGetAllPersistence<E>                     │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📦 Shared Library Interfaces

### 1. `ICrudPersistence<E, ID>`
**Package:** `com.eps.shared.interfaces.persistence`

Định nghĩa các phương thức CRUD cơ bản:

```java
public interface ICrudPersistence<E, ID> {
  // Create/Update
  E save(E entity);
  <T extends E> List<T> saveAll(Iterable<T> entities);

  // Read
  Optional<E> findById(ID id);
  default E findByIdOrNull(ID id) { return findById(id).orElse(null); }
  boolean existsById(ID id);
  List<E> findAllByIdIn(Iterable<ID> ids);

  // Delete
  void deleteById(ID id);
  void deleteByIdIn(Iterable<ID> id);
  void delete(E entity);
  void deleteAllByIdIn(Iterable<? extends ID> ids);
  void deleteAll(Iterable<? extends E> entities);
}
```

### 2. `IJpaGetAllPersistence<E>`
**Package:** `com.eps.shared.interfaces.persistence`

Hỗ trợ phân trang và Specification:

```java
public interface IJpaGetAllPersistence<E> {
  Page<E> findAll(@Nullable Specification<E> spec, Pageable pageable);
}
```

### 3. `IBasePersistence<E, ID>`
**Package:** `com.eps.shared.interfaces.persistence`

**Composite interface** kết hợp CRUD và phân trang:

```java
public interface IBasePersistence<E, ID>
    extends ICrudPersistence<E, ID>, IJpaGetAllPersistence<E> {}
```

### 4. `ICrudPersistenceProvider<E, ID>`
**Package:** `com.eps.shared.interfaces.persistence`

Provider pattern để lấy persistence instance:

```java
public interface ICrudPersistenceProvider<E, ID> {
  ICrudPersistence<E, ID> getCrudPersistence();
}
```

### 5. `IBaseRepository<E, ID>`
**Package:** `com.eps.shared.interfaces.repository`

Kế thừa Spring Data JPA:

```java
public interface IBaseRepository<E, ID>
    extends JpaRepository<E, ID>, JpaSpecificationExecutor<E> {}
```

---

## 🔧 Query Framework (Dynamic SQL Builder)

### Package: `com.eps.shared.services.query`

Framework này cho phép xây dựng dynamic query một cách **type-safe** và **fluent**.

### Core Components

#### 1. `JpaFactory`
**Factory pattern** cung cấp EntityManager singleton:

```java
@Component
public class JpaFactory {
  @Getter private static EntityManager entityManager;

  @Autowired
  public void setEntityManager(EntityManager entityManager) {
    JpaFactory.entityManager = entityManager;
  }
}
```

#### 2. `Functions` (Static Utilities)
Các factory methods để tạo Table, Column, và Aggregate functions:

```java
public class Functions {
  // Table & Column creation
  public static Table table(String name);
  public static Table table(Class<?> entityClass);  // Dùng SimpleName
  public static <T> Column<T> column(String name);

  // Join
  public static JoinExpression join(JoinType type, String tableName);

  // Aggregate functions
  public static <T> Column<T> count(String name);
  public static <T> Column<T> count(ImmutableColumn column);
  public static <T> Column<T> sum(String/ImmutableColumn);
  public static <T> Column<T> avg(String/ImmutableColumn);
  public static <T> Column<T> min(String/ImmutableColumn);
  public static <T> Column<T> max(String/ImmutableColumn);
}
```

#### 3. `Table` & `ImmutableTable`
Đại diện cho bảng trong query:

```java
// Tạo table với alias
ImmutableTable userTable = Functions.table(UserAccount.class).as("u");

// Lấy column từ table
Column<UUID> idCol = userTable.column("id");       // => "u.id"
Column<String> nameCol = userTable.column("name"); // => "u.name"

// Tạo join expression
JoinExpression leftJoin = userTable.leftJoin();
JoinExpression innerJoin = userTable.innerJoin();
```

#### 4. `Column<T>`
Đại diện cho cột, hỗ trợ **fluent condition building**:

```java
Column<T> column = table.column("fieldName");

// Comparison operators
column.equal(value);              // = value
column.equal(otherColumn);        // = other_column
column.notEqual(value);           // <> value
column.greaterThan(value);        // > value
column.greaterThanOrEqual(value); // >= value
column.lessThan(value);           // < value
column.lessThanOrEqual(value);    // <= value

// Collection operators
column.in(Collection<T>);         // IN (...)
column.notIn(Collection<T>);      // NOT IN (...)

// String operators
column.like(pattern);             // LIKE ?

// Null checks
column.isNull();                  // IS NULL
column.isNotNull();               // IS NOT NULL
```

#### 5. `Condition`
Đại diện cho WHERE clause, hỗ trợ **AND/OR chaining**:

```java
Condition c1 = column1.equal(value1);
Condition c2 = column2.greaterThan(value2);

// Chaining
Condition combined = c1.and(c2);    // (c1 AND c2)
Condition either = c1.or(c2);       // (c1 OR c2)

// Complex nesting
Condition complex = c1.and(c2).or(c3);  // ((c1 AND c2) OR c3)
```

**Operators enum:**

| Operator | Format SQL |
|----------|------------|
| `equal` | `= %s` |
| `notEqual` | `<> %s` |
| `greaterThan` | `> %s` |
| `lessThan` | `< %s` |
| `greaterOrEqual` | `>= %s` |
| `lessOrEqual` | `<= %s` |
| `like` | `LIKE ?` |
| `in` | `IN (%s)` |
| `notIn` | `NOT IN (%s)` |
| `isNull` | `IS NULL` |
| `isNotNull` | `IS NOT NULL` |

#### 6. `JoinExpression`
Đại diện cho JOIN clause:

```java
ImmutableTable groupTable = Functions.table(UserGroup.class).as("g");
ImmutableTable relTable = Functions.table(RelUserGroupUser.class).as("rel");

JoinExpression joinExpr = relTable.leftJoin()
    .on(groupTable.column("id").equal(relTable.column("groupId")));
```

#### 7. `QueryCommand` (Builder Pattern)
Xây dựng và thực thi query:

```java
QueryCommand query = QueryCommand.builder()
    .select(userTable.column("id"), userTable.column("name"))
    .from(userTable)
    .join(relTable.leftJoin().on(condition))
    .where(condition1, condition2)      // AND multiple conditions
    .group(userTable.column("orgId"))
    .having(countCondition)
    .build();

// Thực thi query
Stream<Object> results = query.getResultStream();

// Với phân trang và sorting
Stream<Object> pagedResults = query.getResultStream(pageable);
```

**QueryCommand features:**
- Tự động bind parameters (named parameters)
- Hỗ trợ native SQL (`setNative(true)`)
- Pagination và Sorting thông qua `Pageable`
- GROUP BY và HAVING
- Multiple JOINs

---

## 📝 Pattern thực tế

### 1. Domain Persistence Interface

```java
// com.eps.user.domain.services.persistence
public interface UserAccountPersistence extends IBasePersistence<UserAccount, UUID> {
  // Standard queries
  List<UserAccount> findAll();
  Optional<UserAccount> findById(UUID userId);
  UserAccount save(UserAccount entity);

  // Domain-specific queries
  List<UserAccount> findByOrgId(UUID orgId);
  List<UserAccount> findByUnlockAtBeforeAndStatusNot(LocalDateTime now, UserStatus status);

  // Existence check
  Boolean existsByUserCode(String userCode);
  Optional<UserAccount> findByUserCode(String keycloakUsername);

  // Dynamic query (QueryCommand)
  List<UUID> queryUsers(QueryRequest queries);
  List<UserAccount> getUsersByGroupId(UUID groupId);
}
```

### 2. Repository Implementation

```java
// com.eps.user.infrastructure.persistence.repositories
@Repository
public interface NguoiDungRepository
    extends IBaseRepository<UserAccount, UUID>, UserAccountPersistence {

  // Spring Data JPA auto-implement: findByOrgId, findByUserCode, existsByUserCode...

  // Custom implementation bằng default method
  @Override
  default List<UUID> queryUsers(QueryRequest queryRequest) {
    ImmutableTable userTable = Functions.table(UserAccount.class).as("u");

    QueryCommand.Builder builder = QueryCommand.builder()
        .select(userTable.column("id"))
        .from(userTable);

    // Type conversion
    Object rawValue = queryRequest.getValue();
    Object typedValue = convertToTypedValue(rawValue);

    // Build condition từ Operator enum
    Condition condition = queryRequest.getOperator()
        .build(userTable.column("id"), typedValue);

    builder.where(condition);

    return ((Stream<Object>) builder.build().getResultStream())
        .map(o -> ((Tuple) o).get(0, UUID.class))
        .collect(Collectors.toList());
  }

  private Object convertToTypedValue(Object rawValue) {
    if (rawValue instanceof Collection<?> list) {
      if (!list.isEmpty() && list.iterator().next() instanceof String) {
        return list.stream()
            .map(s -> UUID.fromString((String) s))
            .toList();
      }
      return list;
    } else if (rawValue instanceof String s) {
      return UUID.fromString(s);
    }
    return rawValue;
  }
}
```

### 3. Query với JOIN và mapping DTO

```java
@Override
default List<SubData> getGroupIdsByUser(UUID userId) {
  ImmutableTable groupTable = Functions.table(UserGroup.class).as("g");
  ImmutableTable relTable = Functions.table(RelUserGroupUser.class).as("rel");

  QueryCommand query = QueryCommand.builder()
      .select(
          groupTable.column("id"),
          groupTable.column("code"),
          groupTable.column("name")
      )
      .from(groupTable)
      .join(relTable.innerJoin().on(
          groupTable.column("id").equal(relTable.column("groupId"))
      ))
      .where(relTable.column("userId").equal(userId))
      .build();

  return ((Stream<Object>) query.getResultStream())
      .map(o -> {
        Tuple tuple = (Tuple) o;
        return SubData.builder()
            .id(tuple.get(0, UUID.class))
            .code(tuple.get(1, String.class))
            .name(tuple.get(2, String.class))
            .build();
      })
      .collect(Collectors.toList());
}
```

### 4. Aggregate Query

```java
default Map<UUID, Long> countUsersByOrg() {
  ImmutableTable userTable = Functions.table(UserAccount.class).as("u");

  QueryCommand query = QueryCommand.builder()
      .select(
          userTable.column("orgId"),
          Functions.count(userTable.column("id")).as("total")
      )
      .from(userTable)
      .group(userTable.column("orgId"))
      .build();

  return ((Stream<Object>) query.getResultStream())
      .collect(Collectors.toMap(
          o -> ((Tuple) o).get(0, UUID.class),
          o -> ((Tuple) o).get(1, Long.class)
      ));
}
```

---

## 🎯 Operator Enum (Dynamic Query)

`Operator` nằm trong domain layer, hỗ trợ JSON serialization:

```java
public enum Operator {
  @JsonProperty("==") EQUAL("==") {
    @Override
    public Condition build(Column<?> column, Object value) {
      return column.equal(value);
    }
  },
  @JsonProperty("IN") IN("IN") {
    @Override
    public Condition build(Column<?> column, Object value) {
      return column.in((Collection) value);
    }
  },
  // ... other operators

  public abstract Condition build(Column<?> column, Object value);
}
```

---

## 📋 Service Layer Transaction

```java
@Service
@RequiredArgsConstructor
public class UserAccountService {

  private final UserAccountPersistence userPersistence;

  @Transactional(readOnly = true)
  public List<UserAccount> findByOrg(UUID orgId) {
    return userPersistence.findByOrgId(orgId);
  }

  @Transactional
  public UserAccount create(CreateUserCommand command) {
    UserAccount user = UserAccount.create(command);
    return userPersistence.save(user);
  }
}
```

> **Note:** `@Transactional` đặt ở **Service layer**, Repository chỉ chứa query logic.

---

## 📁 File Locations

| Component | Package |
|-----------|---------|
| ICrudPersistence | `com.eps.shared.interfaces.persistence` |
| IBasePersistence | `com.eps.shared.interfaces.persistence` |
| IJpaGetAllPersistence | `com.eps.shared.interfaces.persistence` |
| IBaseRepository | `com.eps.shared.interfaces.repository` |
| QueryCommand | `com.eps.shared.services.query.models` |
| Table, Column, Condition | `com.eps.shared.services.query.models` |
| Functions | `com.eps.shared.services.query.models` |
| JpaFactory | `com.eps.shared.services.query` |
| ImmutableTable, ImmutableColumn | `com.eps.shared.services.query.interfaces` |

---

## ✅ Best Practices

1. **Domain Persistence Interface:** Chỉ định nghĩa contract, không có implementation logic
2. **Repository:** Dùng `default` methods cho custom queries với QueryCommand
3. **QueryCommand:** Ưu tiên dùng cho dynamic queries phức tạp, JOINs, aggregations
4. **Spring Data JPA:** Tận dụng method naming convention cho simple queries
5. **Type Safety:** Luôn convert String → UUID trước khi query
6. **Parameterized Queries:** QueryCommand tự động bind parameters, tránh SQL injection

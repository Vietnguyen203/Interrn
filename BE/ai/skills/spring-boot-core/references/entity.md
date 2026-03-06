# Entity Convention

## Overview
Entities trong `domain/models/entities` kế thừa `AuditingEntity` từ shared library, dùng JPA annotations và Lombok.

## Structure
- Package: `domain/models/entities`
- Extends: `AuditingEntity` (shared)
- Annotations:
  - `@Entity`
  - `@Table(name = "{entity_lower}")`
  - `@Getter @Setter @AllArgsConstructor @NoArgsConstructor` (Lombok)

## Fields Convention
- ID: `UUID` (kế thừa từ `AuditingEntity`)
- Audit fields: `createdAt`, `updatedAt`, `createdBy`, `updatedBy` (từ `AuditingEntity`)
- Business fields:
  - `name`, `code` (nếu có)
  - `status` (enum)
  - Foreign keys: `orgId`, `personId`, v.v.

## Column Naming
- Snake case: `user_code`, `phone_number`, `unlock_at`
- Dùng `@Column(name = "...")` để map

## Relationships
- OneToOne/ManyToOne: `@JoinColumn` với `updatable = false, insertable = false` nếu chỉ đọc
- Optional relationships: `@NotFound(action = NotFoundAction.IGNORE)`

## Enums trong dự án (IEnum + Converter)
Dự án đang dùng pattern:

- Enum **implements `IEnum`** và trả về `byte getValue()`
- DB lưu enum dạng **`TINYINT/NUMBER(3)`** (Byte)
- Dùng JPA `AttributeConverter` generic từ shared: `com.eps.shared.interfaces.converter.IConverter`
- Mỗi enum cần **1 converter class** ở `infrastructure/persistence/coverters` với `@Converter(autoApply = true)` để tự động convert.

### 1) Enum implements IEnum
Ví dụ thực tế: `AuthOperation`, `UserStatus`, `UserType`:
```java
public enum AuthOperation implements IEnum {
  LOGIN((byte) 0),
  RESET_PASSWORD((byte) 1);

  private final byte value;

  AuthOperation(byte value) {
    this.value = value;
  }

  @Override
  public byte getValue() {
    return value;
  }
}
```

### 2) Converter để inject/convert enum vào DB
Ví dụ thực tế: `AuthOperationConverter`:
```java
@Converter(autoApply = true)
public class AuthOperationConverter implements IConverter<AuthOperation> {}
```

### 3) Shared generic converter (IConverter)
`IConverter<E extends Enum<E> & IEnum> implements AttributeConverter<E, Byte>`:
- `convertToDatabaseColumn()` -> `attribute.getValue()`
- `convertToEntityAttribute()` -> `EnumUtils.fromValue(enumClass, dbData)`

## Template
- Entity: `assets/templates/entity-template.java`
- Enum: `assets/templates/enum-template.java`
- Converter: sẽ cần template riêng nếu bạn muốn generate nhanh (mình có thể bổ sung).
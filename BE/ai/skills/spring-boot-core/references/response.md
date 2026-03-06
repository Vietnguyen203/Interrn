# Response DTO Convention (theo backend/user-service)

## Vị trí
`backend/user-service/src/main/java/com/eps/user/application/responses/*`

## Pattern theo code thật
Ví dụ: `UserAccountPageResponse`.

## 1) Lombok + cấu trúc class
- `@Data` (phổ biến)

```java
@Data
public class UserAccountPageResponse {
  private UUID id;
  private String name;
  private String userCode;
  private UserStatus status;
  private UserType type;
  // ...
}
```

## 2) Nội dung DTO
- DTO thường là dạng "flatten" để frontend dùng trực tiếp
- Có thể bổ sung field từ quan hệ (ví dụ `orgName`) thay vì embed entity

Theo code thật:
- Có `orgId`, `orgName`
- Có auditing fields: `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

## 3) Naming convention
- `{Entity}PageResponse`: dùng cho list/paging
- `{Entity}Response`: dùng cho get detail (tuỳ module)

## 4) Mapping
Mapping thường được thực hiện trong service layer (usecase):
- Dùng `FnCommon.copyProperties(dto, entity)` (shared)
- Nếu cần custom field (ví dụ from relationship) thì set thêm

Ví dụ thực tế trong `UserAccountService.mappingResponse()`:
- copy entity -> dto
- nếu `entity.getOrganization() != null` thì set `orgId`, `orgName`

## Template
- Tham khảo file thật:
  - `backend/user-service/src/main/java/com/eps/user/application/responses/UserAccountPageResponse.java`
- Template trong skill:
  - `assets/templates/response-template.java` (cần placeholders theo entity)

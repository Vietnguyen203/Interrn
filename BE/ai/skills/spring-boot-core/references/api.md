# API Layer Convention

## Overview
API layer trong `user-service` tuân theo pattern **Controller implements Base API interface** từ shared library, giúp tái sử dụng CRUD logic và giữ endpoint nhất quán.

## Controller Structure
- Package: `adapter/apis`
- Base annotations:
  - `@RestController`
  - `@RequestMapping(path = "/{entity-lower}")`
  - `@RequiredArgsConstructor` (Lombok)
- Implements: `IBaseApi<Entity, UUID, ResponseDTO, RequestDTO, PageResponseDTO>`

## Standard CRUD Endpoints
Khi implements `IBaseApi`, controller tự động có các endpoint:
- `POST /` (create)
- `GET /{id}`
- `GET /` (page + filter)
- `PUT /{id}` (update)
- `DELETE /{id}`

## Custom Endpoints
- Thường là `@PatchMapping` hoặc `@GetMapping` với path tùy chỉnh
- Gọi trực tiếp service method
- Ví dụ:
  - `@PatchMapping("/{id}/status/{status}")`
  - `@GetMapping("/search")`
  - `@GetMapping("/{id}/groups")`

## Authorization Header Injection
Khi cần truyền Authorization vào base flow:
```java
@Override
public ResponseEntity<ResponseDTO> create(HeaderContext context, Map<String, Object> headers, RequestDTO request) {
    context.getExtraData().put(HttpHeaders.AUTHORIZATION, (String) headers.get(HeaderKeys.AUTHORIZATION));
    return IBaseApi.super.create(context, headers, request);
}
```

## Request Validation
- Sử dụng `@Valid` trên `@RequestBody`
- Validation logic được đẩy xuống `Validator` class

## Response Mapping
- Base CRUD trả về `PageResponse<T>` tự động
- Custom endpoint trả về DTO hoặc `ResponseEntity` tùy ý

## Filter Query Pattern
Endpoint search thường nhận `filter` string JSON, parse bằng `JsonParserUtils.entity(filter.trim(), Map.class)`

## Cross-cutting
- Logging qua `@Slf4j`
- Exception handling qua shared library
- OpenAPI/Swagger tự động từ shared starter

## Template
Xem `assets/templates/controller-template.java` để tạo controller mới theo chuẩn.
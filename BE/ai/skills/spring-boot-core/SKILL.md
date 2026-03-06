---
name: spring-boot-analyze
description: Phân tích và tạo mã nguồn Spring Boot chuẩn theo convention dự án, bao gồm entity, service, controller, repository, request/response, exception handling, logging, validator, schedule, cross-service call và enum converter.
---

# Spring Boot Analyze & Code Generation

## When to use this skill
Sử dụng skill này khi bạn cần:
- Phân tích cấu trúc và convention của dự án Spring Boot hiện tại
- Tạo mới các thành phần (entity, service, controller, repository, v.v.) theo chuẩn dự án
- Hiểu quy tắc đặt tên, package, annotation và pattern đang được áp dụng
- Tích hợp enum với JPA converter để lưu dưới dạng TINYINT
- Xử lý exception, logging, validator theo chuẩn unified
- Gọi API cross-service giữa các microservices
- Tạo schedule job với @Scheduled

## Available references (load on demand)

### Core Components
- **Entity Convention** (`references/entity.md`) - Tạo entity kế thừa AuditingEntity, dùng JPA annotations, Lombok, enum với IConverter
- **Service Logic** (`references/logic-service.md`) - Pattern cho service layer, dependency injection, transaction management
- **Repository Pattern** (`references/database-call.md`) - JPA repository, query methods, pagination, sorting
- **Controller API** (`references/api.md`) - REST controller, request/response mapping, validation
- **Request/Response DTOs** (`references/request.md`, `references/response.md`) - DTO pattern, validation annotations

### Infrastructure & Cross-cutting
- **Exception Handling** (`references/exception-handling.md`) - Global exception handler, custom exceptions, error response format
- **Logging** (`references/log.md`) - Logback config, logging levels, structured logging
- **Validator** (`references/validator.md`) - Custom validators, Bean Validation, error messages
- **Schedule Jobs** (`references/schedule.md`) - @Scheduled patterns, cron expressions, job management
- **Cross-Service Calls** (`references/cross-service.md`) - FeignClient/WebClient, service discovery, circuit breaker

### Code Generation Templates
Templates sẵn có trong `assets/templates/`:
- `entity-template.java` - Entity với AuditingEntity
- `service-template.java` - Service với dependency injection
- `controller-template.java` - REST controller
- `repository-template.java` - JPA repository
- `request-template.java` / `response-template.java` - DTO templates
- `enum-template.java` - Enum implements IEnum
- `main-application-template.java` - Spring Boot main class
- `application-template.yml` - Application configuration
- `logback-template.xml` - Logging configuration

## Task → References mapping

### Tạo API CRUD mới
Đọc theo thứ tự:
1. `references/api.md` - Controller pattern
2. `references/request.md` - Request DTO
3. `references/response.md` - Response DTO
4. `references/logic-service.md` - Service logic
5. `references/database-call.md` - Repository
6. `references/validator.md` - Validation rules
7. `references/exception-handling.md` - Error handling

### Tạo Entity mới với Enum
Đọc theo thứ tự:
1. `references/entity.md` - Entity convention + enum pattern
2. `assets/templates/enum-template.java` - Enum template
3. `assets/templates/entity-template.java` - Entity template

### Tạo Schedule Job
Đọc theo thứ tự:
1. `references/schedule.md` - @Scheduled patterns
2. `references/log.md` - Logging trong job
3. `references/exception-handling.md` - Error handling

### Gọi Cross-Service API
Đọc theo thứ tự:
1. `references/cross-service.md` - FeignClient/WebClient
2. `references/exception-handling.md` - Circuit breaker, timeout
3. `references/log.md` - Logging cho external calls

### Phân tích convention dự án
Đọc theo thứ tự:
1. `references/entity.md` - Entity + package structure
2. `references/api.md` - API pattern
3. `references/log.md` - Logging convention
4. `references/exception-handling.md` - Error format

## Usage pattern
1. Xác định loại tác vụ (API CRUD, Entity, Schedule, Cross-service, v.v.)
2. Đọc các references tương ứng theo mapping bên trên
3. Sử dụng templates trong `assets/templates/` để generate code
4. Tuân thủ unified convention từ shared library

## Notes
- Chỉ load references khi cần để tránh context bloat
- Templates đảm bảo consistency across components
- Enum values lưu dưới dạng byte trong database với IConverter pattern
- API responses follow unified error format
# Setup & yêu cầu trước khi chạy

## 1) Prerequisites

- JDK (để chạy Gradle + Liquibase)
- Quyền truy cập DB tương ứng theo môi trường (dev/qa/test/...)

## 2) Cấu hình kết nối theo module/env

Mỗi module quản lý config riêng tại:

`backend/migration-service/modules/<module>/config/<env>/liquibase.properties`

Ví dụ (PostgreSQL):

```properties
url=jdbc:postgresql://localhost:5432/postgresql
username=admin
password=123456
driver=org.postgresql.Driver
logLevel=info
defaultSchemaName=public
```

## 3) Cách chạy chuẩn

```bash
cd backend/migration-service
./gradlew migrate-<module> -Penv=<env> -Ptag=<tag>
```

Nếu cần filter:

```bash
./gradlew migrate-<module> -Penv=<env> -Ptag=<tag> -Pfilter="1.00 or 202602041015"
```

Rollback:

```bash
./gradlew rollback-<module> -Penv=<env> -Ptag=<tag>
```


# Gradle tasks & parameters (migration-service)

Tài liệu này mô tả đúng các tasks/params đang được định nghĩa trong `backend/migration-service/build.gradle`.

## Mục tiêu chính

- Chạy Liquibase update theo **từng module**: `migrate-<module>`
- Rollback theo **từng module** về **tag**: `rollback-<module>`
- Chạy update toàn bộ modules: `migrateAll`
- Rollback toàn bộ modules về **tag**: `rollbackAll`

## Tham số dùng chung

- **`-Penv=<env>`**: chọn môi trường/config folder cho module.
  - Repo đang có nhiều dạng env folder, ví dụ: `dev`, `qa`, `test`, `postgresql`, `oracle`, `mssql`, và một số module có `dev-1`.
  - Script sẽ đọc `modules/<module>/config/<env>/liquibase.properties`.
- **`-Ptag=<tag>`**:
  - Với `migrate-<module>`: nếu có `-Ptag`, Gradle sẽ chạy `tag <tag>` sau khi `update`.
  - Với `rollback-<module>` và `rollbackAll`: **bắt buộc** có `-Ptag`.
- **`-Pfilter="<labels expression>"`**: chạy chọn lọc theo labels (map sang Liquibase `--labels`).
  - Expression có thể dùng `or`, `and`, và `!` (phủ định), theo README của repo.

## migrate theo module

```bash
cd backend/migration-service
./gradlew migrate-<module> -Penv=<env> -Ptag=<tag>
```

Ví dụ:

```bash
./gradlew migrate-file -Penv=postgresql -Ptag=202602041015
```

### migrate theo labels filter

```bash
./gradlew migrate-user -Penv=dev -Ptag=1.00 -Pfilter="1.00 or 202602041015"
```

## rollback theo module

```bash
cd backend/migration-service
./gradlew rollback-<module> -Penv=<env> -Ptag=<tag>
```

Ví dụ:

```bash
./gradlew rollback-user -Penv=dev -Ptag=202602041015
```

## migrateAll

```bash
cd backend/migration-service
./gradlew migrateAll -Penv=dev -Ptag=1.00
```

> Lưu ý: `migrateAll` là task aggregate, nó depends-on `migrate-<module>` cho tất cả module.

## rollbackAll

```bash
cd backend/migration-service
./gradlew rollbackAll -Penv=dev -Ptag=1.00
```

## Ghi chú kỹ thuật quan trọng (để debug)

- File `build.gradle` sẽ:
  - Auto-discover modules bằng cách list directories trong `modules/`
  - Tạo `databaseChangeLogTableName` và `databaseChangeLogLockTableName` theo module để tách lịch sử migration giữa các module
  - Tự detect driver từ `url` (sqlserver/oracle/postgresql)


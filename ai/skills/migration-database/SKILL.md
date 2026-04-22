---
name: migration-database
description: Hướng dẫn triển khai và vận hành database migration theo chuẩn dự án (Liquibase + Gradle) trong `backend/migration-service`. Dùng khi cần thêm changelog mới cho một module, chạy migrate/rollback theo môi trường (dev/qa/test/postgresql/oracle/mssql), gắn tag, hoặc lọc changeset bằng labels.
license: MIT
metadata:
  author: eps-team
  version: "1.0"
---

# Migration Database (Liquibase + Gradle)

Skill này đóng gói cách dự án đang làm **database migration** bằng **Liquibase** trong `backend/migration-service`.

## Khi nào sử dụng

- Thêm changelog mới (tạo bảng/cột/index, sửa data, refactor schema) cho một module trong `backend/migration-service/modules/*`
- Chạy migrate theo module hoặc chạy toàn bộ modules
- Rollback về một tag cụ thể
- Cần filter migration theo `labels` (ví dụ chạy theo version hoặc theo timestamp)
- Troubleshoot các lỗi Liquibase phổ biến (lock, checksum, sai env, sai driver)

## Quick start (thực tế theo repo)

### 1) Tạo changelog mới cho một module

- Chọn module: ví dụ `modules/user-registration/`
- Tạo file trong `modules/<module>/changelog/` theo chuẩn tên file của repo:
  - `YYYYMMDDHHmm-<mô-tả-ngắn>.xml` (ví dụ: `202602041015-add-column-foo.xml`)
- Trong `<changeSet>`:
  - **`id`**: nên unique và “self-descriptive” (repo thường dùng dạng `YYYYMMDDHHmm-...`)
  - **`labels`**: bắt buộc, thường trùng timestamp hoặc version
  - **`rollback`**: bắt buộc (repo yêu cầu rollback cho migration file)

Xem template & ví dụ trong: [references/CHANGELOG_TEMPLATES.md](references/CHANGELOG_TEMPLATES.md)

### 2) Chạy migrate theo module

Đi tới `backend/migration-service` và chạy:

- Migrate 1 module:
  - `./gradlew migrate-<module> -Penv=<env> -Ptag=<tag>`
  - Ví dụ: `./gradlew migrate-file -Penv=postgresql -Ptag=202602041015`

- Migrate có filter labels (map sang `--labels`):
  - `./gradlew migrate-<module> -Penv=<env> -Ptag=<tag> -Pfilter="<expr>"`
  - Ví dụ: `./gradlew migrate-user -Penv=dev -Ptag=1.00 -Pfilter="1.00 or 202602041015"`

Chi tiết tasks & parameters: [references/GRADLE_TASKS.md](references/GRADLE_TASKS.md)

### 3) Rollback theo module / rollbackAll

- Rollback 1 module về tag:
  - `./gradlew rollback-<module> -Penv=<env> -Ptag=<tag>`
  - Ví dụ: `./gradlew rollback-user -Penv=dev -Ptag=202602041015`

- Rollback toàn bộ:
  - `./gradlew rollbackAll -Penv=<env> -Ptag=<tag>`

## Chuẩn cấu trúc (repo)

Mỗi module nằm trong `backend/migration-service/modules/<module>/` thường có:

- `db.changelog-master.xml` (đang dùng `<includeAll path="changelog" relativeToChangelogFile="true"/>`)
- `changelog/*.xml` (các changeset file)
- `config/<env>/liquibase.properties` (url/username/password/driver/defaultSchemaName)

Xem conventions: [references/CONVENTIONS.md](references/CONVENTIONS.md)

## Checklist khi thêm migration mới

- [ ] File nằm đúng module: `backend/migration-service/modules/<module>/changelog/`
- [ ] Tên file theo timestamp + mô tả ngắn
- [ ] `<changeSet>` có `labels="..."` (dùng timestamp hoặc version)
- [ ] Kiểu dữ liệu & độ dài theo conventions (`name` 255 NVARCHAR, `code` 100 VARCHAR, `description` 1024 NVARCHAR, `jsonData` CLOB, còn lại mặc định 255)
- [ ] Đặt tên constraint/index đúng format (`nn/u q/pk/fk/idx-<table>-<col>`)
- [ ] Có `<rollback>` (đặc biệt với drop/rename/data migration)
- [ ] Chạy migrate trên env mục tiêu `-Penv=...` đúng DB
- [ ] Tag sau khi update bằng `-Ptag=...` để rollback được
- [ ] Nếu cần chạy chọn lọc, dùng `-Pfilter="..."` đúng expression

## Troubleshooting nhanh

Xem: [references/TROUBLESHOOTING.md](references/TROUBLESHOOTING.md)

## Tài liệu tham khảo trong repo

- `backend/migration-service/readme.md`
- `backend/migration-service/build.gradle`

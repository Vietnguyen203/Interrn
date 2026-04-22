# Troubleshooting (Liquibase migration-service)

## 1) Chạy sai env (không tìm thấy liquibase.properties)

**Triệu chứng**

- Warning kiểu “Configuration file not found: modules/<module>/config/<env>/liquibase.properties”

**Cách xử lý**

- Kiểm tra folder config thật sự có tồn tại cho module đó:
  - `backend/migration-service/modules/<module>/config/<env>/liquibase.properties`
- Dùng đúng `-Penv=<env>` (ví dụ `dev`, `qa`, `test`, `postgresql`, `oracle`, `mssql`, hoặc `dev-1` nếu module có)

## 2) Sai driver / URL

**Triệu chứng**

- Liquibase báo không load được driver hoặc lỗi JDBC URL

**Cách xử lý**

- Mở `modules/<module>/config/<env>/liquibase.properties` và kiểm tra `url`
- Repo có auto-detect driver từ `url` (postgresql/oracle/sqlserver). Nếu URL không khớp pattern, driver có thể rỗng.

## 3) Rollback fail vì thiếu tag

**Triệu chứng**

- Task rollback báo `Tag is required. Use -Ptag=your_tag_name`

**Cách xử lý**

- Bắt buộc truyền `-Ptag=<tag>`
- Lưu ý: `migrate-<module>` sẽ chỉ `tag` DB nếu bạn truyền `-Ptag` lúc migrate

## 4) Filter labels không chạy đúng (không apply changeset)

**Triệu chứng**

- Chạy `-Pfilter=...` nhưng không thấy changeset mới được apply

**Cách xử lý**

- Kiểm tra changeset có `labels="..."` hay chưa
- Kiểm tra biểu thức filter:
  - `1.00 or 202602041015`
  - `1.00 and 202602041015`
  - `1.00 and !202602041016`

## 5) Liquibase lock (changelog lock table)

**Triệu chứng**

- Liquibase báo database đang bị lock bởi session khác

**Cách xử lý**

- Xác định lock table của module:
  - Repo set `DATABASECHANGELOGLOCK` theo module, dạng `<MODULE>_CHANGELOG_LOCK` (upper-case)
- Xoá lock đúng bảng (cẩn thận), sau khi đảm bảo không còn process migrate khác đang chạy.

## 6) Checksum mismatch

**Triệu chứng**

- Liquibase báo checksum changed cho changeset đã chạy

**Nguyên nhân thường gặp**

- Bạn sửa lại file changeset đã được apply ở môi trường đó

**Cách xử lý (khuyến nghị)**

- Tránh sửa changeset đã chạy; tạo changeset mới để sửa/điều chỉnh
- Nếu bắt buộc, cần xử lý theo quy trình của team (clearCheckSums / update DATABASECHANGELOG) — nên làm có kiểm soát vì ảnh hưởng lịch sử migration


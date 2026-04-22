# Conventions & cấu trúc module (Liquibase)

Tài liệu này mô tả conventions đang được dùng trong `backend/migration-service`.

## Cấu trúc module

```
backend/migration-service/modules/<module>/
├── db.changelog-master.xml
├── changelog/
│   ├── <nhiều-file>.xml
└── config/
    ├── dev/liquibase.properties
    ├── qa/liquibase.properties
    ├── test/liquibase.properties
    ├── postgresql/liquibase.properties
    ├── oracle/liquibase.properties
    └── mssql/liquibase.properties
```

Một số module có thêm env khác (ví dụ `dev-1`). Khi chạy, `-Penv=<env>` phải trỏ đúng folder tồn tại.

## Master changelog

Trong repo, `db.changelog-master.xml` của module thường dùng:

- `<includeAll path="changelog" relativeToChangelogFile="true"/>`

=> Chỉ cần thêm file mới vào `changelog/` là được include tự động.

## Naming conventions cho file changelog

- **Tên file**: `YYYYMMDDHHmm-<mô-tả-ngắn>.xml`
  - Ví dụ: `202601061700-alter-auditing.xml`
- **Khuyến nghị**:
  - Mô tả ngắn, dùng dấu `-`, tránh ký tự đặc biệt
  - Một file có thể chứa 1 hoặc nhiều changeset, nhưng repo thường làm “1 file ~ 1 mục đích”

## ChangeSet conventions

Repo đang dùng các field sau:

- **`id`**: unique; thường trùng hoặc bắt đầu bằng timestamp + mô tả
- **`author`**: username
- **`labels`**: bắt buộc
  - Dùng để filter chọn lọc khi migrate qua `-Pfilter` (map sang Liquibase `--labels`)
  - Thực tế repo hay dùng timestamp y hệt tên file, hoặc version (ví dụ `1.00`)

## Quy ước kiểu dữ liệu & độ dài mặc định

- Trừ khi có quy định riêng, **string mặc định `NVARCHAR(255)`**.
- Các trường chuẩn:
  - `name`: `NVARCHAR(255)`
  - `code`: `VARCHAR(100)`
  - `description`: `NVARCHAR(1024)`
  - `jsonData`: `CLOB` (Liquibase set `type="CLOB"`, trong entity dùng `@Lob`)

## Quy ước đặt tên constraint/index

- `nn-<table_name>-<column_name>`: NOT NULL constraint
- `uq-<table_name>-<column_name>`: UNIQUE constraint
- `pk-<table_name>-<column_name>`: PRIMARY KEY
- `fk-<table_name>-<column_name>`: FOREIGN KEY
- `idx-<table_name>-<column_name1>-<column_name2>`: index (nhiều cột nối bằng `-`)

> Luôn đặt tên constraint/index trong changelog theo format trên để đồng bộ AI sinh mã.

## Rollback

Repo có yêu cầu rollback trong README, và nhiều changelog file có rollback rõ ràng (create/drop, add/drop, rename back, v.v).

**Best practice cho repo này**:

- Nếu change là destructive (drop/rename) => rollback phải khôi phục được (hoặc có note nếu không thể).
- Data migration (SQL) => rollback cần cân nhắc khả năng đảo ngược.

## Liquibase properties

Trong `modules/<module>/config/<env>/liquibase.properties` thường có:

- `url=...`
- `username=...`
- `password=...`
- `driver=...`
- `logLevel=info`
- `defaultSchemaName=...` (optional nhưng repo có dùng)

> Repo hiện **không** cố định `changeLogFile` trong mọi properties; `build.gradle` mặc định dùng `modules/<module>/db.changelog-master.xml` nếu thiếu.


# Changelog templates (Liquibase XML)

Các template dưới đây bám theo format đang xuất hiện trong repo `backend/migration-service/modules/*/changelog/*.xml`.

## 1) Template tạo bảng + rollback drop

```xml
<?xml version="1.0" encoding="utf-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="
       http://www.liquibase.org/xml/ns/dbchangelog
       http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

    <changeSet id="202602041015-create-table-example" author="yourname" labels="202602041015">
        <createTable tableName="example">
            <column name="id" type="uuid">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="name" type="NVARCHAR(255)"/>
            <column name="created_by" type="nvarchar(255)"/>
            <column name="updated_by" type="nvarchar(255)"/>
            <column name="created_at" type="timestamp"/>
            <column name="updated_at" type="timestamp"/>
        </createTable>

        <rollback>
            <dropTable tableName="example"/>
        </rollback>
    </changeSet>

</databaseChangeLog>
```

## 2) Template rename column (refactor) + rollback rename back

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="202602041020-rename-column-example" author="yourname" labels="202602041020">
        <renameColumn tableName="example" oldColumnName="old_name" newColumnName="new_name"/>

        <rollback>
            <renameColumn tableName="example" oldColumnName="new_name" newColumnName="old_name"/>
        </rollback>
    </changeSet>

</databaseChangeLog>
```

## 3) Template drop column + rollback add column

```xml
<?xml version="1.0" encoding="utf-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="
       http://www.liquibase.org/xml/ns/dbchangelog
       http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

    <changeSet id="202602041030-drop-column-example" author="yourname" labels="202602041030">
        <dropColumn tableName="example" columnName="legacy_col"/>

        <rollback>
            <addColumn tableName="example">
                <column name="legacy_col" type="VARCHAR2(255)"/>
            </addColumn>
        </rollback>
    </changeSet>

</databaseChangeLog>
```

## 4) Template “data migration” (sql) + rollback

Data migration thường khó rollback hoàn toàn. Nếu rollback không khả thi, cần note rõ.

```xml
<?xml version="1.0" encoding="utf-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="
       http://www.liquibase.org/xml/ns/dbchangelog
       http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

    <changeSet id="202602041040-backfill-example" author="yourname" labels="202602041040">
        <sql>
            UPDATE example SET name = 'unknown' WHERE name IS NULL;
        </sql>

        <rollback>
            <!-- Nếu không thể rollback an toàn, hãy mô tả lý do -->
            <comment>Rollback not supported safely for this data migration.</comment>
        </rollback>
    </changeSet>

</databaseChangeLog>
```

## 5) Lưu ý về labels (để chạy filter)

Repo chạy migrate có thể truyền `-Pfilter=...` để map sang `--labels`.

Nên dùng:

- `labels="<timestamp>"` để chọn theo thời gian
- hoặc `labels="<version>"` để gom theo release (ví dụ `1.00`)


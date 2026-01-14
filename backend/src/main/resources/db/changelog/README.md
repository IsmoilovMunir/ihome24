# Liquibase Migration Guide

## Структура проекта

Проект использует **YAML формат** для master changelog и **SQL формат** для миграций.

```
db/changelog/
├── db.changelog-master.yaml         # Главный файл (YAML формат)
└── changeset/
    ├── products_V001__initial.sql              # Создание таблицы products
    ├── categories_V001__initial.sql            # Создание таблицы categories
    ├── products_V002__add_category_id.sql      # Изменение таблицы products
    └── ...
```

## Правила именования файлов

Формат: `{table_name}_V{номер}__{описание}.sql`

Примеры:
- `products_V001__initial.sql` - создание таблицы products
- `products_V002__add_category_id.sql` - добавление колонки в products
- `user_V001__initial.sql` - создание таблицы user
- `user_V002__update_user_add_profile_pic.sql` - обновление таблицы user

## Как добавить новую миграцию

### Шаг 1: Создайте SQL файл

Создайте файл в папке `changeset/` с правильным именем:

**Пример:** `user_V001__initial.sql`

```sql
--liquibase formatted sql

--changeset ihome24:user_V001__initial
--comment: Создание таблицы user

CREATE TABLE user (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_user_email ON user(email);

--rollback DROP TABLE IF EXISTS user CASCADE;
```

### Шаг 2: Добавьте в master changelog

Откройте `db.changelog-master.yaml` и добавьте в конец:

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/changeset/products_V001__initial.sql
  - include:
      file: db/changelog/changeset/user_V001__initial.sql  # ← Добавьте здесь
```

**Важно:** Сохраняйте порядок! Новые миграции добавляются в конец списка.

### Шаг 3: Запустите приложение

При запуске Spring Boot автоматически применит новую миграцию.

## Важные принципы

1. ✅ **Старые таблицы сохраняются автоматически** - `drop-first: false` в `application.yml`
2. ✅ Каждый changeset в **отдельном SQL файле**
3. ✅ Используйте **последовательную нумерацию** (V001, V002, V003...)
4. ✅ **Никогда не изменяйте** уже примененные changeset файлы
5. ✅ Для изменений создавайте **новые changeset файлы** с увеличенным номером версии
6. ✅ **Всегда указывайте rollback** в SQL файлах

## Примеры операций

### Создать таблицу
```sql
--liquibase formatted sql
--changeset ihome24:table_V001__initial
CREATE TABLE table_name (...);
--rollback DROP TABLE IF EXISTS table_name CASCADE;
```

### Добавить колонку
```sql
--liquibase formatted sql
--changeset ihome24:table_V002__add_column
ALTER TABLE table_name ADD COLUMN new_column VARCHAR(255);
--rollback ALTER TABLE table_name DROP COLUMN IF EXISTS new_column;
```

### Создать индекс
```sql
--liquibase formatted sql
--changeset ihome24:table_V003__add_index
CREATE INDEX idx_table_name_column ON table_name(column);
--rollback DROP INDEX IF EXISTS idx_table_name_column;
```

## Конфигурация

В `application.yml`:

```yaml
spring:
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml
    drop-first: false  # ✅ Гарантирует сохранение данных!
```

## Проверка миграций

```bash
# Посмотреть SQL без применения
mvn liquibase:updateSQL

# Статус миграций
mvn liquibase:status

# Применить миграции
mvn liquibase:update
```

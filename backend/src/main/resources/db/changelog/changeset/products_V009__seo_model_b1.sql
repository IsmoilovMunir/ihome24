--liquibase formatted sql

--changeset ihome24:products_V009__rename_seo_columns
--comment: B-1 — колонки slug, meta_title, meta_description по ТЗ
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'products' AND column_name = 'seo_slug'

ALTER TABLE products RENAME COLUMN seo_slug TO slug;
ALTER TABLE products RENAME COLUMN seo_meta_title TO meta_title;
ALTER TABLE products RENAME COLUMN seo_meta_description TO meta_description;

--changeset ihome24:products_V009__og_image
--comment: B-1 — og_image для Open Graph
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'products' AND column_name = 'og_image'

ALTER TABLE products ADD COLUMN og_image VARCHAR(500);

--changeset ihome24:products_V009__slug_indexes
--comment: B-1 — уникальный индекс и индекс для поиска по slug

DROP INDEX IF EXISTS uk_products_seo_slug;
DROP INDEX IF EXISTS idx_products_seo_slug;

CREATE UNIQUE INDEX IF NOT EXISTS uk_products_slug ON products(slug);
CREATE INDEX IF NOT EXISTS idx_products_slug ON products(slug);

--changeset ihome24:products_V009__slug_backfill_sql
--comment: B-1 — временный slug для строк без значения (уточняется Java backfill)

UPDATE products
SET slug = 'product-' || id
WHERE slug IS NULL OR TRIM(slug) = '';

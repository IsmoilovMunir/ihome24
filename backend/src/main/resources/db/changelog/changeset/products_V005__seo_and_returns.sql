--liquibase formatted sql

--changeset ihome24:products_V005__seo_and_returns
--comment: SEO-поля и настройки возвратов для товаров

ALTER TABLE products ADD COLUMN IF NOT EXISTS seo_slug VARCHAR(120);
ALTER TABLE products ADD COLUMN IF NOT EXISTS seo_meta_title VARCHAR(255);
ALTER TABLE products ADD COLUMN IF NOT EXISTS seo_meta_description VARCHAR(500);
ALTER TABLE products ADD COLUMN IF NOT EXISTS returns_allowed BOOLEAN DEFAULT true;
ALTER TABLE products ADD COLUMN IF NOT EXISTS returns_days INTEGER DEFAULT 14;
ALTER TABLE products ADD COLUMN IF NOT EXISTS returns_conditions TEXT;

CREATE INDEX IF NOT EXISTS idx_products_seo_slug ON products(seo_slug) WHERE seo_slug IS NOT NULL;

--rollback ALTER TABLE products DROP COLUMN IF EXISTS seo_slug, DROP COLUMN IF EXISTS seo_meta_title, DROP COLUMN IF EXISTS seo_meta_description, DROP COLUMN IF EXISTS returns_allowed, DROP COLUMN IF EXISTS returns_days, DROP COLUMN IF EXISTS returns_conditions;

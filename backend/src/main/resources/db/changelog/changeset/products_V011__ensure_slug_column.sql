--liquibase formatted sql

--changeset ihome24:products_V011__ensure_slug_column
--comment: Prod recovery — колонка slug и SEO-поля, если старые миграции были пропущены (MARK_RAN)

ALTER TABLE products ADD COLUMN IF NOT EXISTS slug VARCHAR(100);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema() AND table_name = 'products' AND column_name = 'seo_slug'
    ) THEN
        EXECUTE $q$
            UPDATE products
            SET slug = seo_slug
            WHERE (slug IS NULL OR TRIM(slug) = '')
              AND seo_slug IS NOT NULL AND TRIM(seo_slug) <> ''
        $q$;
    END IF;
END $$;

UPDATE products
SET slug = 'product-' || id
WHERE slug IS NULL OR TRIM(slug) = '';

--changeset ihome24:products_V011__ensure_meta_columns
--comment: Переименование seo_meta_* → meta_* если ещё не сделано

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'products' AND column_name = 'seo_meta_title')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'products' AND column_name = 'meta_title') THEN
        ALTER TABLE products RENAME COLUMN seo_meta_title TO meta_title;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'products' AND column_name = 'seo_meta_description')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'products' AND column_name = 'meta_description') THEN
        ALTER TABLE products RENAME COLUMN seo_meta_description TO meta_description;
    END IF;
END $$;

ALTER TABLE products ADD COLUMN IF NOT EXISTS meta_title VARCHAR(255);
ALTER TABLE products ADD COLUMN IF NOT EXISTS meta_description VARCHAR(500);
ALTER TABLE products ADD COLUMN IF NOT EXISTS og_image VARCHAR(500);

CREATE UNIQUE INDEX IF NOT EXISTS uk_products_slug ON products(slug);
CREATE INDEX IF NOT EXISTS idx_products_slug ON products(slug);

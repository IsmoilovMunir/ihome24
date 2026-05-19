--liquibase formatted sql

--changeset ihome24:products_V006__seo_slug_unique_redirects
--comment: Уникальный seo_slug (100 символов) и 301-редиректы со старых slug

ALTER TABLE products ALTER COLUMN seo_slug TYPE VARCHAR(100);

CREATE UNIQUE INDEX IF NOT EXISTS uk_products_seo_slug ON products(seo_slug) WHERE seo_slug IS NOT NULL;

CREATE TABLE IF NOT EXISTS product_slug_redirects (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    slug VARCHAR(100) NOT NULL,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_product_slug_redirects_slug UNIQUE (slug)
);

CREATE INDEX IF NOT EXISTS idx_product_slug_redirects_product_id ON product_slug_redirects(product_id);

--rollback DROP TABLE IF EXISTS product_slug_redirects; DROP INDEX IF EXISTS uk_products_seo_slug;

--liquibase formatted sql

--changeset ihome24:products_V010__slug_redirects_old_new
--comment: B-4 — product_slug_redirects: old_slug → new_slug (без цепочек)

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'product_slug_redirects' AND column_name = 'slug'

ALTER TABLE product_slug_redirects ADD COLUMN IF NOT EXISTS new_slug VARCHAR(100);

UPDATE product_slug_redirects r
SET new_slug = p.slug
FROM products p
WHERE r.product_id = p.id AND (r.new_slug IS NULL OR r.new_slug = '');

ALTER TABLE product_slug_redirects RENAME COLUMN slug TO old_slug;

ALTER TABLE product_slug_redirects DROP CONSTRAINT IF EXISTS uk_product_slug_redirects_slug;
ALTER TABLE product_slug_redirects DROP COLUMN IF EXISTS product_id;

ALTER TABLE product_slug_redirects ALTER COLUMN new_slug SET NOT NULL;

ALTER TABLE product_slug_redirects ADD CONSTRAINT uk_product_slug_redirects_old_slug UNIQUE (old_slug);

CREATE INDEX IF NOT EXISTS idx_product_slug_redirects_new_slug ON product_slug_redirects(new_slug);

--rollback SELECT 1;

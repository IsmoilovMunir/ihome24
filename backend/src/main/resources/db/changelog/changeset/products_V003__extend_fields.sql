--liquibase formatted sql

--changeset ihome24:products_V003__extend_fields
--comment: Расширение таблицы products новыми полями для полной поддержки формы добавления товара
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name='products' AND column_name='barcode'

-- Добавление новых колонок
ALTER TABLE products ADD COLUMN barcode VARCHAR(100);
ALTER TABLE products ADD COLUMN tax_enabled BOOLEAN DEFAULT false;
ALTER TABLE products ADD COLUMN status VARCHAR(50) DEFAULT 'draft';
ALTER TABLE products ADD COLUMN supplier_id BIGINT;
ALTER TABLE products ADD COLUMN collection_id BIGINT;
ALTER TABLE products ADD COLUMN discounted_price DECIMAL(10,2);

-- Создание индексов
CREATE INDEX idx_products_barcode ON products(barcode);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_supplier_id ON products(supplier_id);
CREATE INDEX idx_products_collection_id ON products(collection_id);

-- Добавление внешних ключей (только если таблицы существуют)
--preconditions onFail:CONTINUE
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.tables WHERE table_name='suppliers'
ALTER TABLE products 
    ADD CONSTRAINT fk_products_supplier 
    FOREIGN KEY (supplier_id) 
    REFERENCES suppliers(id) 
    ON DELETE SET NULL;

--preconditions onFail:CONTINUE
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.tables WHERE table_name='collections'
ALTER TABLE products 
    ADD CONSTRAINT fk_products_collection 
    FOREIGN KEY (collection_id) 
    REFERENCES collections(id) 
    ON DELETE SET NULL;

-- Уникальное ограничение для barcode
CREATE UNIQUE INDEX uk_products_barcode ON products(barcode) WHERE barcode IS NOT NULL;

--rollback ALTER TABLE products DROP COLUMN IF EXISTS barcode, DROP COLUMN IF EXISTS tax_enabled, DROP COLUMN IF EXISTS status, DROP COLUMN IF EXISTS supplier_id, DROP COLUMN IF EXISTS collection_id, DROP COLUMN IF EXISTS discounted_price CASCADE;

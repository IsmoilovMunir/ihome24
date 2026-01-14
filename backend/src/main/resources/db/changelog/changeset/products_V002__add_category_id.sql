--liquibase formatted sql

--changeset ihome24:products_V002__add_category_id
--comment: Добавление колонки category_id в таблицу products для связи с категориями

-- Добавление новой колонки
ALTER TABLE products 
    ADD COLUMN category_id BIGINT;

-- Создание индекса для новой колонки
CREATE INDEX idx_products_category_id ON products(category_id);

-- Добавление внешнего ключа
ALTER TABLE products 
    ADD CONSTRAINT fk_products_category 
    FOREIGN KEY (category_id) 
    REFERENCES categories(id) 
    ON DELETE SET NULL;

--rollback ALTER TABLE products DROP COLUMN IF EXISTS category_id CASCADE;

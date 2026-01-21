--liquibase formatted sql

--changeset ihome24:product_variants_V001__initial
--comment: Создание таблицы product_variants для хранения вариантов товаров

CREATE TABLE product_variants (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    product_id BIGINT NOT NULL,
    option_name VARCHAR(100) NOT NULL,
    option_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_product_variants_product 
        FOREIGN KEY (product_id) 
        REFERENCES products(id) 
        ON DELETE CASCADE
);

CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);
CREATE INDEX idx_product_variants_option_name ON product_variants(option_name);

--rollback DROP TABLE IF EXISTS product_variants CASCADE;

--liquibase formatted sql

--changeset ihome24:products_V001__initial
--comment: Создание таблицы products для хранения товаров

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    old_price DECIMAL(10,2),
    sku VARCHAR(100),
    stock_quantity INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    is_featured BOOLEAN DEFAULT false,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Индексы для оптимизации запросов
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_is_active ON products(is_active);
CREATE INDEX idx_products_is_featured ON products(is_featured);

-- Уникальное ограничение для SKU
CREATE UNIQUE INDEX uk_products_sku ON products(sku) WHERE sku IS NOT NULL;

--rollback DROP TABLE IF EXISTS products CASCADE;

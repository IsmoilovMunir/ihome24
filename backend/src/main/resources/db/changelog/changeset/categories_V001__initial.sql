--liquibase formatted sql

--changeset ihome24:categories_V001__initial
--comment: Создание таблицы categories для категорий товаров

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_id BIGINT,
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT true NOT NULL,
    sort_order INTEGER DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Внешний ключ для иерархии категорий
ALTER TABLE categories 
    ADD CONSTRAINT fk_categories_parent 
    FOREIGN KEY (parent_id) 
    REFERENCES categories(id) 
    ON DELETE SET NULL;

-- Индексы
CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_is_active ON categories(is_active);

--rollback DROP TABLE IF EXISTS categories CASCADE;

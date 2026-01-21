--liquibase formatted sql

--changeset ihome24:collections_V001__initial
--comment: Создание таблицы collections для хранения коллекций товаров

CREATE TABLE collections (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_collections_slug ON collections(slug);
CREATE INDEX idx_collections_is_active ON collections(is_active);

--rollback DROP TABLE IF EXISTS collections CASCADE;

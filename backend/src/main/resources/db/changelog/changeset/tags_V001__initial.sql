--liquibase formatted sql

--changeset ihome24:tags_V001__initial
--comment: Создание таблицы tags для хранения тегов товаров

CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_tags_slug ON tags(slug);
CREATE INDEX idx_tags_name ON tags(name);

--rollback DROP TABLE IF EXISTS tags CASCADE;

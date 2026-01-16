--liquibase formatted sql

--changeset ihome24:suppliers_V001__initial
--comment: Создание таблицы suppliers для хранения поставщиков

CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_suppliers_is_active ON suppliers(is_active);
CREATE INDEX idx_suppliers_name ON suppliers(name);

--rollback DROP TABLE IF EXISTS suppliers CASCADE;

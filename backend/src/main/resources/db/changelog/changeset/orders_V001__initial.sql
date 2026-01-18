--liquibase formatted sql

--changeset ihome24:orders_V001__initial
--comment: Создание таблицы orders для хранения заказов

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    order_number BIGINT NOT NULL UNIQUE,
    customer VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    payment VARCHAR(20) NOT NULL CHECK (payment IN ('PAID', 'PENDING', 'CANCELLED', 'FAILED')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('DELIVERED', 'OUT_FOR_DELIVERY', 'READY_TO_PICKUP', 'DISPATCHED')),
    spent DECIMAL(10,2),
    method VARCHAR(20) NOT NULL CHECK (method IN ('PAYPAL', 'MASTERCARD')),
    method_number VARCHAR(50),
    order_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Индексы для оптимизации запросов
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_customer ON orders(customer);
CREATE INDEX idx_orders_email ON orders(email);
CREATE INDEX idx_orders_payment ON orders(payment);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_date ON orders(order_date);

--rollback DROP TABLE IF EXISTS orders CASCADE;
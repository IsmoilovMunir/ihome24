--liquibase formatted sql

--changeset ihome24:orders_V002__add_order_items
--comment: Добавление order_items и новых полей в orders

-- Новые колонки в orders
ALTER TABLE orders ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivery_method VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS comment TEXT;

-- Расширяем CHECK для method (CASH) и status (PENDING)
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_method_check;
ALTER TABLE orders ADD CONSTRAINT orders_method_check CHECK (method IN ('PAYPAL', 'MASTERCARD', 'CASH'));
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;
ALTER TABLE orders ADD CONSTRAINT orders_status_check CHECK (status IN ('PENDING', 'DELIVERED', 'OUT_FOR_DELIVERY', 'READY_TO_PICKUP', 'DISPATCHED'));

-- Таблица order_items
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    product_name VARCHAR(500) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);

--rollback DROP TABLE IF EXISTS order_items; ALTER TABLE orders DROP COLUMN IF EXISTS phone, DROP COLUMN IF EXISTS address, DROP COLUMN IF EXISTS delivery_method, DROP COLUMN IF EXISTS comment;

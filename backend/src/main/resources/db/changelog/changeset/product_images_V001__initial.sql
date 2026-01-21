--liquibase formatted sql

--changeset ihome24:product_images_V001__initial
--comment: Создание таблицы product_images для хранения изображений товаров

CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    sort_order INTEGER DEFAULT 0,
    is_primary BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_product_images_product 
        FOREIGN KEY (product_id) 
        REFERENCES products(id) 
        ON DELETE CASCADE
);

CREATE INDEX idx_product_images_product_id ON product_images(product_id);
CREATE INDEX idx_product_images_is_primary ON product_images(is_primary);
CREATE INDEX idx_product_images_sort_order ON product_images(sort_order);

--rollback DROP TABLE IF EXISTS product_images CASCADE;

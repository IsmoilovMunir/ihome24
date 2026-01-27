--liquibase formatted sql

--changeset ihome24:file_metadata_V001__initial
--comment: Создание таблицы file_metadata для хранения медиафайлов

CREATE TABLE file_metadata (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    thumbnail_path VARCHAR(500),
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(20) NOT NULL,
    media_size VARCHAR(20),
    product_id BIGINT,
    user_id BIGINT NOT NULL,
    bucket_name VARCHAR(100) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    is_main BOOLEAN DEFAULT false,
    file_group VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_file_metadata_product_id ON file_metadata(product_id);
CREATE INDEX idx_file_metadata_file_type ON file_metadata(file_type);
CREATE INDEX idx_file_metadata_media_size ON file_metadata(media_size);
CREATE INDEX idx_file_metadata_sort_order ON file_metadata(sort_order);
CREATE INDEX idx_file_metadata_group ON file_metadata(file_group);

--rollback DROP TABLE IF EXISTS file_metadata CASCADE;

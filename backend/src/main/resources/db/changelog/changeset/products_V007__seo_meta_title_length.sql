--liquibase formatted sql

--changeset ihome24:products_V007__seo_meta_title_length
--comment: Meta Title — хранение до 120 символов

ALTER TABLE products ALTER COLUMN seo_meta_title TYPE VARCHAR(120);

--rollback ALTER TABLE products ALTER COLUMN seo_meta_title TYPE VARCHAR(255);

--liquibase formatted sql

--changeset ihome24:products_V008__seo_meta_description_length
--comment: Meta Description — хранение до 300 символов

ALTER TABLE products ALTER COLUMN seo_meta_description TYPE VARCHAR(300);

--rollback ALTER TABLE products ALTER COLUMN seo_meta_description TYPE VARCHAR(500);

--liquibase formatted sql

--changeset ihome24:orders_V006__company_extra_fields
--comment: Доп. поля организации покупателя (из DaData)

ALTER TABLE orders ADD COLUMN IF NOT EXISTS company_address TEXT;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS company_ogrn VARCHAR(20);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS company_okpo VARCHAR(20);

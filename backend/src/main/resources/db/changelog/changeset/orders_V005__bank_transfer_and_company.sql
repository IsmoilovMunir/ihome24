--liquibase formatted sql

--changeset ihome24:orders_V005__bank_transfer_and_company
--comment: Оплата по расчётному счёту и реквизиты покупателя (юр. лицо)

ALTER TABLE orders ADD COLUMN IF NOT EXISTS company_name VARCHAR(255);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS company_inn VARCHAR(12);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS company_kpp VARCHAR(9);

ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_method_check;
ALTER TABLE orders ADD CONSTRAINT orders_method_check CHECK (method IN (
    'PAYPAL', 'MASTERCARD', 'CASH', 'BANK_TRANSFER'
));

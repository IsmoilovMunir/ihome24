--liquibase formatted sql

--changeset ihome24:orders_V007__company_bank_fields
--comment: Bank requisites for B2B invoice orders

ALTER TABLE orders ADD COLUMN IF NOT EXISTS company_corr_account VARCHAR(20);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS company_bik VARCHAR(9);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS company_settlement_account VARCHAR(20);

--rollback ALTER TABLE orders DROP COLUMN IF EXISTS company_corr_account;
--rollback ALTER TABLE orders DROP COLUMN IF EXISTS company_bik;
--rollback ALTER TABLE orders DROP COLUMN IF EXISTS company_settlement_account;

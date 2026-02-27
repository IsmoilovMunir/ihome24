--liquibase formatted sql

--changeset ihome24:company_settings_V002__currency
--comment: Add currency_rate and currency_percent_adjustment to company_settings

ALTER TABLE company_settings
    ADD COLUMN IF NOT EXISTS currency_rate NUMERIC(12, 4) DEFAULT 1,
    ADD COLUMN IF NOT EXISTS currency_percent_adjustment NUMERIC(6, 2) DEFAULT 0;

COMMENT ON COLUMN company_settings.currency_rate IS 'Курс валюты: например 80 для 1 USD = 80 RUB';
COMMENT ON COLUMN company_settings.currency_percent_adjustment IS 'Процент коррекции: + при росте доллара, - при снижении (например 2 или -1.5)';

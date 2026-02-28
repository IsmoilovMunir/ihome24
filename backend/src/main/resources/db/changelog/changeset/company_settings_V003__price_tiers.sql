--liquibase formatted sql

--changeset ihome24:company_settings_V003__price_tiers
--comment: Add price_tiers_json for quantity-based price levels (retail, small/large wholesale)

ALTER TABLE company_settings
    ADD COLUMN IF NOT EXISTS price_tiers_json TEXT;

COMMENT ON COLUMN company_settings.price_tiers_json IS 'JSON array of price tiers: [{"minQty":1,"maxQty":10,"discountPercent":0,"label":"Розничная"},...]. maxQty null = no upper limit.';

--rollback ALTER TABLE company_settings DROP COLUMN IF EXISTS price_tiers_json;

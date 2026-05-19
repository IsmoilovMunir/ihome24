--liquibase formatted sql

--changeset ihome24:wholesale_leads_V002
--comment: Status and manager assignment for wholesale leads

ALTER TABLE wholesale_leads
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'NEW';

ALTER TABLE wholesale_leads
    ADD COLUMN IF NOT EXISTS assigned_manager_id BIGINT;

ALTER TABLE wholesale_leads
    ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMP;

ALTER TABLE wholesale_leads
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT NOW();

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_wholesale_leads_manager'
    ) THEN
        ALTER TABLE wholesale_leads
            ADD CONSTRAINT fk_wholesale_leads_manager
                FOREIGN KEY (assigned_manager_id) REFERENCES users (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_wholesale_leads_status ON wholesale_leads (status);
CREATE INDEX IF NOT EXISTS idx_wholesale_leads_manager ON wholesale_leads (assigned_manager_id);

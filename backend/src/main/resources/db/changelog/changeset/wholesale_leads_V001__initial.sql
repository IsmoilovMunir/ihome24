--liquibase formatted sql

--changeset ihome24:wholesale_leads_V001
--comment: Store wholesale lead form submissions for Telegram bot

CREATE TABLE IF NOT EXISTS wholesale_leads (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    inn VARCHAR(20),
    message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wholesale_leads_created_at ON wholesale_leads (created_at DESC);

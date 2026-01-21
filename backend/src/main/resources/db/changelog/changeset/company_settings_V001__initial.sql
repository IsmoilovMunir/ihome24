--liquibase formatted sql

--changeset ihome24:company_settings_V001__initial
--comment: Create company_settings table for storing company information

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'company_settings') THEN
        CREATE TABLE company_settings (
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            inn VARCHAR(20),
            ogrn VARCHAR(20),
            kpp VARCHAR(20),
            country VARCHAR(100) NOT NULL DEFAULT 'Russia',
            legal_address VARCHAR(500),
            actual_address VARCHAR(500),
            phone VARCHAR(50),
            email VARCHAR(100),
            website VARCHAR(255),
            director_name VARCHAR(255),
            accountant_name VARCHAR(255),
            bank_name VARCHAR(255),
            bank_account VARCHAR(50),
            correspondent_account VARCHAR(50),
            bik VARCHAR(20),
            is_active BOOLEAN NOT NULL DEFAULT TRUE,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        );

        CREATE INDEX idx_company_settings_is_active ON company_settings(is_active);
        CREATE INDEX idx_company_settings_country ON company_settings(country);
    END IF;
END $$;

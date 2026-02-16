--liquibase formatted sql

--changeset ihome24:products_V004__quantity_per_package
--comment: Количество в упаковке (для отображения на карточке товара)
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name='products' AND column_name='quantity_per_package'

ALTER TABLE products ADD COLUMN quantity_per_package INTEGER;

--rollback ALTER TABLE products DROP COLUMN IF EXISTS quantity_per_package;

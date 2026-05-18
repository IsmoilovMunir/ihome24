--liquibase formatted sql

--changeset ihome24:orders_V004__add_preliminary_status
--comment: Статус PRELIMINARY — незавершённое оформление (контакты без оплаты)

ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;
ALTER TABLE orders ADD CONSTRAINT orders_status_check CHECK (status IN (
    'PRELIMINARY',
    'PENDING',
    'IN_PROCESSING',
    'DISPATCHED',
    'OUT_FOR_DELIVERY',
    'READY_TO_PICKUP',
    'DELIVERED'
));

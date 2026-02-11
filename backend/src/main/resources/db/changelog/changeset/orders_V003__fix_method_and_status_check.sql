--liquibase formatted sql

--changeset ihome24:orders_V003__fix_method_and_status_check
--comment: Расширение CHECK для method (CASH) и status (IN_PROCESSING) для продакшена

-- method: добавляем CASH (если на проде остался старый constraint только PAYPAL, MASTERCARD)
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_method_check;
ALTER TABLE orders ADD CONSTRAINT orders_method_check CHECK (method IN ('PAYPAL', 'MASTERCARD', 'CASH'));

-- status: добавляем IN_PROCESSING (полный список из Order.OrderStatus)
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;
ALTER TABLE orders ADD CONSTRAINT orders_status_check CHECK (status IN ('PENDING', 'IN_PROCESSING', 'DISPATCHED', 'OUT_FOR_DELIVERY', 'READY_TO_PICKUP', 'DELIVERED'));

delete from order_product;

delete from orders;

-- 1) orders 테이블에 total_price 컬럼 추가
ALTER TABLE orders
    ADD COLUMN total_price DECIMAL(15, 2) NOT NULL DEFAULT 0;

INSERT INTO orders (id, user_id, status, order_date, total_price)
VALUES
    (1, 1, 'COMPLETED', '2025-11-01 10:00:00', 65700),
    (2, 1, 'PENDING',   '2025-11-10 12:30:00', 50800),
    (3, 2, 'CANCELED',  '2025-11-15 09:15:00', 39900),
    (4, 2, 'COMPLETED', '2025-11-20 14:20:00', 119700);

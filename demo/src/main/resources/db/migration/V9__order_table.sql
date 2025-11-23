ALTER TABLE product
    ADD COLUMN stock_status VARCHAR(20) NOT NULL DEFAULT 'IN_STOCK';

ALTER TABLE users
    ADD COLUMN address VARCHAR(255) NOT NULL DEFAULT 'UNKNOWN';

ALTER TABLE users
    MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP;

INSERT INTO users (username, email, password_hash, address) VALUES
    ('jiyeon', 'jy@test.com', '1234', 'Seoul'),
    ('mina', 'mina@test.com', '5678', 'Busan');

-- 주문 테이블
CREATE TABLE orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        status VARCHAR(20) NOT NULL,   -- PENDING / COMPLETED / CANCELED
                        order_date DATETIME NOT NULL,
                        CONSTRAINT fk_orders_users
                            FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 주문-상품 매핑 테이블
CREATE TABLE order_product (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               order_id BIGINT NOT NULL,
                               product_id BIGINT NOT NULL,
                               order_price DECIMAL(15, 2) NOT NULL,
                               count INT NOT NULL,
                               CONSTRAINT fk_order_product_order
                                   FOREIGN KEY (order_id) REFERENCES orders(id),
                               CONSTRAINT fk_order_product_product
                                   FOREIGN KEY (product_id) REFERENCES product(id)
);

-- 주문 더미 데이터
INSERT INTO orders (id, user_id, status, order_date)
VALUES
    (1, 1, 'COMPLETED', '2025-11-01 10:00:00'),
    (2, 1, 'PENDING',   '2025-11-10 12:30:00'),
    (3, 2, 'CANCELED',  '2025-11-15 09:15:00'),
    (4, 2, 'COMPLETED', '2025-11-20 14:20:00');

INSERT INTO order_product (id, order_id, product_id, order_price, count)
VALUES
    (1, 1, 100, 12900, 2),
    (2, 1, 108, 34900, 1);

INSERT INTO order_product (id, order_id, product_id, order_price, count)
VALUES
    (3, 2, 104, 39900, 1),
    (4, 2, 204, 15900, 1);

INSERT INTO order_product (id, order_id, product_id, order_price, count)
VALUES
    (5, 3, 200, 39900, 1);

INSERT INTO order_product (id, order_id, product_id, order_price, count)
VALUES
    (6, 4, 107, 59900, 1),
    (7, 4, 207, 29900, 2);

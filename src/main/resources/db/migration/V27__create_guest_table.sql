CREATE TABLE guests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    password_hash VARCHAR(255) NOT NULL,
    -- 주문자 정보
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    -- 배송지 정보
    zipcode VARCHAR(10) NOT NULL,
    city VARCHAR(100) NOT NULL,
    street VARCHAR(255) NOT NULL,
    detail VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- guest_password 삭제
ALTER TABLE orders DROP COLUMN guest_password;

-- order_number 추가
ALTER TABLE orders ADD COLUMN order_number VARCHAR(50) UNIQUE NOT NULL;

-- guest_id 추가
ALTER TABLE orders ADD COLUMN guest_id BIGINT;
ALTER TABLE orders ADD CONSTRAINT fk_orders_guest
    FOREIGN KEY (guest_id) REFERENCES guests(id);
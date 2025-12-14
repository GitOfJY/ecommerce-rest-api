CREATE TABLE delivery_addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,              -- 회원이면 값, 비회원이면 NULL
    recipient_name VARCHAR(100) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE, -- 기본 배송지 여부
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- orders 테이블 수정
ALTER TABLE orders ADD COLUMN delivery_address_id BIGINT;
ALTER TABLE orders ADD COLUMN guest_password VARCHAR(255);
ALTER TABLE orders MODIFY user_id BIGINT NULL;
ALTER TABLE orders ADD FOREIGN KEY (delivery_address_id) REFERENCES delivery_addresses(id);
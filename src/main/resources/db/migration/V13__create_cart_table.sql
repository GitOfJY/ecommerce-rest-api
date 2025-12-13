ALTER TABLE product RENAME TO products;
ALTER TABLE category RENAME TO categories;

CREATE TABLE carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE cart_products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (cart_id) REFERENCES carts(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    UNIQUE (cart_id, product_id)  -- 같은 상품 중복 방지
);

-- cart 더미데이터
-- 카트 생성 (user_id = 9)
INSERT INTO carts (user_id, created_at, updated_at)
VALUES (9, NOW(), NOW());

-- 카트 상품 추가 (cart_id = 1 이라고 가정)
INSERT INTO cart_products (cart_id, product_id, quantity, created_at, updated_at)
VALUES
    (1, 1001, 2, NOW(), NOW()),
    (1, 1003, 1, NOW(), NOW()),
    (1, 1005, 1, NOW(), NOW());
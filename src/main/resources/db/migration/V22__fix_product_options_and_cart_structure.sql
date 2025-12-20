-- 1. FK 체크 비활성화
SET FOREIGN_KEY_CHECKS = 0;

-- 2. 테이블 완전 재생성
DROP TABLE IF EXISTS cart_products;
DROP TABLE IF EXISTS product_options;

-- 3. product_options 재생성
CREATE TABLE product_options (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    color VARCHAR(50) NOT NULL,
    size VARCHAR(20) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    additional_price DECIMAL(13,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uk_product_color_size (product_id, color, size)
);

-- 4. cart_products 재생성
CREATE TABLE cart_products (
     id BIGINT PRIMARY KEY AUTO_INCREMENT,
     cart_id BIGINT NOT NULL,
     product_id BIGINT NOT NULL,
     selected_color VARCHAR(50) NULL,
     selected_size VARCHAR(20) NULL,
     quantity INT NOT NULL DEFAULT 1,
     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
     updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
     FOREIGN KEY (product_id) REFERENCES products(id),
     UNIQUE KEY uk_cart_product_option (cart_id, product_id, selected_color, selected_size)
);

-- 5. FK 체크 재활성화
SET FOREIGN_KEY_CHECKS = 1;
-- product_options 테이블 생성
CREATE TABLE IF NOT EXISTS product_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    color VARCHAR(50) NOT NULL,
    size VARCHAR(20) NOT NULL,
    additional_price DECIMAL(13, 2) NOT NULL DEFAULT 0,
    stock INT NOT NULL DEFAULT 0,
    stock_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_option_product
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,

    CONSTRAINT uk_product_color_size
        UNIQUE (product_id, color, size)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

# -- products 테이블에 has_options 컬럼 추가
# ALTER TABLE products
#     ADD COLUMN IF NOT EXISTS has_options BOOLEAN NOT NULL DEFAULT FALSE;

-- cart_products에 product_option_id 컬럼 추가
# ALTER TABLE cart_products
#     ADD COLUMN product_option_id BIGINT NULL,
#     ADD CONSTRAINT fk_cart_product_option
#         FOREIGN KEY (product_option_id) REFERENCES product_options(id) ON DELETE CASCADE;

-- 같은 장바구니에 같은 상품+옵션 조합은 하나만
# ALTER TABLE cart_products
#     ADD CONSTRAINT uk_cart_product_option
#         UNIQUE (cart_id, product_id, product_option_id);

INSERT INTO product_options (product_id, color, size, additional_price, stock, stock_status) VALUES
    (1001, '빨강', 'S', 0, 10, 'IN_STOCK'),
    (1001, '빨강', 'M', 0, 15, 'IN_STOCK'),
    (1001, '빨강', 'L', 1000, 8, 'IN_STOCK'),
    (1001, '빨강', 'XL', 2000, 3, 'LOW_STOCK'),
    (1001, '파랑', 'S', 0, 20, 'IN_STOCK'),
    (1001, '파랑', 'M', 0, 12, 'IN_STOCK'),
    (1001, '파랑', 'L', 1000, 5, 'LOW_STOCK'),
    (1001, '파랑', 'XL', 2000, 0, 'OUT_OF_STOCK'),
    (1001, '검정', 'S', 500, 18, 'IN_STOCK'),
    (1001, '검정', 'M', 500, 25, 'IN_STOCK'),
    (1001, '검정', 'L', 1500, 7, 'IN_STOCK'),
    (1001, '검정', 'XL', 2500, 2, 'LOW_STOCK');
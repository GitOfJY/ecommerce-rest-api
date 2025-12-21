-- V23__migrate_products_to_options.sql

-- 1. products 테이블에 stock, stock_status가 있는지 확인
SET @stock_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'spring_db'
      AND TABLE_NAME = 'products'
      AND COLUMN_NAME = 'stock'
);

-- 2. stock 컬럼이 있으면 데이터 이동
SET @sql = IF(@stock_exists > 0,
              'INSERT INTO product_options (product_id, color, size, stock, stock_status, additional_price, created_at, updated_at)
               SELECT id, ''기본'', ''FREE'', stock, stock_status, 0.00, NOW(), NOW()
               FROM products
               WHERE has_options = 0',
              'SELECT ''Stock column does not exist, skipping data migration'' AS message'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. stock 컬럼이 있으면 제거
SET @sql = IF(@stock_exists > 0,
              'ALTER TABLE products DROP COLUMN stock',
              'SELECT ''Stock column already removed'' AS message'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. stock_status 컬럼이 있으면 제거
SET @status_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'spring_db'
      AND TABLE_NAME = 'products'
      AND COLUMN_NAME = 'stock_status'
);

SET @sql = IF(@status_exists > 0,
              'ALTER TABLE products DROP COLUMN stock_status',
              'SELECT ''Stock_status column already removed'' AS message'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. order_product 테이블 정리
TRUNCATE TABLE order_product;

-- 6. product_option_id 컬럼 추가 (없으면)
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'spring_db'
      AND TABLE_NAME = 'order_product'
      AND COLUMN_NAME = 'product_option_id'
);

SET @sql = IF(@col_exists = 0,
              'ALTER TABLE order_product ADD COLUMN product_option_id BIGINT NOT NULL AFTER product_id',
              'SELECT ''product_option_id already exists'' AS message'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 7. 외래키 추가 (없으면)
SET @fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'spring_db'
      AND TABLE_NAME = 'order_product'
      AND CONSTRAINT_NAME = 'fk_order_product_option'
);

SET @sql = IF(@fk_exists = 0,
              'ALTER TABLE order_product ADD CONSTRAINT fk_order_product_option FOREIGN KEY (product_option_id) REFERENCES product_options(id)',
              'SELECT ''Foreign key already exists'' AS message'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 8. 인덱스 추가 (없으면)
SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'spring_db'
      AND TABLE_NAME = 'order_product'
      AND INDEX_NAME = 'idx_order_product_option'
);

SET @sql = IF(@idx_exists = 0,
              'CREATE INDEX idx_order_product_option ON order_product(product_option_id)',
              'SELECT ''Index already exists'' AS message'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
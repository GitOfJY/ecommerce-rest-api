-- 1. product_options에 stock_status 컬럼 추가
ALTER TABLE product_options ADD COLUMN stock_status VARCHAR(20) NOT NULL DEFAULT 'IN_STOCK' AFTER stock;

-- 2. products에서 stock_status 컬럼 삭제
ALTER TABLE products DROP COLUMN stock_status;

-- 3. order_product에 product_option_id 추가 (없으면)
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'spring_db'
      AND TABLE_NAME = 'order_product'
      AND COLUMN_NAME = 'product_option_id'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE order_product ADD COLUMN product_option_id BIGINT NULL AFTER product_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- product_option_id 컬럼 삭제
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'spring_db'
      AND TABLE_NAME = 'order_product'
      AND COLUMN_NAME = 'product_option_id'
);

SET @sql = IF(@col_exists > 0,
              'ALTER TABLE order_product DROP COLUMN product_option_id',
              'SELECT 1'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
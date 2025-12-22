-- stock 컬럼만 처리 (stock_status 없으면 무시)
SET @stock_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'spring_db'
      AND TABLE_NAME = 'products'
      AND COLUMN_NAME = 'stock'
);

SET @sql = IF(@stock_exists > 0,
    'ALTER TABLE products DROP COLUMN stock',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- order_product 테이블 정리
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
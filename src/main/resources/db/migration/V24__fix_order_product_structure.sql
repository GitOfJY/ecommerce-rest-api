-- 1. FK 존재 여부 확인 후 삭제
SET @fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'spring_db'
      AND TABLE_NAME = 'order_product'
      AND CONSTRAINT_NAME = 'fk_order_product_option'
);

SET @sql = IF(@fk_exists > 0,
              'ALTER TABLE order_product DROP FOREIGN KEY fk_order_product_option',
              'SELECT 1'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. FK 체크 비활성화 후 데이터 삭제
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE order_product;
SET FOREIGN_KEY_CHECKS = 1;

-- 3. product_option_id 컬럼 삭제
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

-- 4. selected_color 컬럼 추가
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'spring_db'
      AND TABLE_NAME = 'order_product'
      AND COLUMN_NAME = 'selected_color'
);

SET @sql = IF(@col_exists = 0,
              'ALTER TABLE order_product ADD COLUMN selected_color VARCHAR(50) NULL AFTER product_id',
              'SELECT 1'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. selected_size 컬럼 추가
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'spring_db'
      AND TABLE_NAME = 'order_product'
      AND COLUMN_NAME = 'selected_size'
);

SET @sql = IF(@col_exists = 0,
              'ALTER TABLE order_product ADD COLUMN selected_size VARCHAR(20) NULL AFTER selected_color',
              'SELECT 1'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
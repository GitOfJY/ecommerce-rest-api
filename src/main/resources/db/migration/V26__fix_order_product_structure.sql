# -- 2. 기존 데이터 삭제 (테스트 환경)
# TRUNCATE TABLE order_product;
#
# -- 3. product_option_id 컬럼 삭제
# ALTER TABLE order_product
#     DROP FOREIGN KEY fk_order_product_option;
#
# ALTER TABLE order_product
#     DROP COLUMN product_option_id;
#
# -- 4. selected_color, selected_size 컬럼 추가
# ALTER TABLE order_product
#     ADD COLUMN selected_color VARCHAR(50) NULL AFTER product_id,
#     ADD COLUMN selected_size VARCHAR(20) NULL AFTER selected_color;

-- V26__fix_order_product_structure.sql

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
              'SELECT ''FK already removed'' AS message'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. product_option_id 컬럼 존재 여부 확인 후 삭제
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'spring_db'
      AND TABLE_NAME = 'order_product'
      AND COLUMN_NAME = 'product_option_id'
);

-- 2-1. FK 체크 비활성화 후 데이터 삭제
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE order_product;
SET FOREIGN_KEY_CHECKS = 1;

-- 2-2. 컬럼 삭제
SET @sql = IF(@col_exists > 0,
              'ALTER TABLE order_product DROP COLUMN product_option_id',
              'SELECT ''Column already removed'' AS message'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. selected_color 컬럼 추가
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'spring_db'
      AND TABLE_NAME = 'order_product'
      AND COLUMN_NAME = 'selected_color'
);

SET @sql = IF(@col_exists = 0,
              'ALTER TABLE order_product ADD COLUMN selected_color VARCHAR(50) NULL AFTER product_id',
              'SELECT ''selected_color already exists'' AS message'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. selected_size 컬럼 추가
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'spring_db'
      AND TABLE_NAME = 'order_product'
      AND COLUMN_NAME = 'selected_size'
);

SET @sql = IF(@col_exists = 0,
              'ALTER TABLE order_product ADD COLUMN selected_size VARCHAR(20) NULL AFTER selected_color',
              'SELECT ''selected_size already exists'' AS message'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. 인덱스 추가
SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'spring_db'
      AND TABLE_NAME = 'order_product'
      AND INDEX_NAME = 'idx_order_product_option'
);

SET @sql = IF(@idx_exists = 0,
              'CREATE INDEX idx_order_product_option ON order_product(product_id, selected_color, selected_size)',
              'SELECT ''Index already exists'' AS message'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
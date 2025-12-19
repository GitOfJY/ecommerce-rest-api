-- 1. users 테이블에 상태 관련 컬럼 추가
ALTER TABLE users
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '회원 상태: ACTIVE, WITHDRAWN, BANNED';

ALTER TABLE users
    ADD COLUMN withdrawn_at TIMESTAMP NULL COMMENT '탈퇴 일시';

-- 2. 인덱스 추가 (선택사항)
CREATE INDEX idx_user_status ON users(status);

-- 3. 테스트 데이터 초기화 (개발 환경에만!)
-- 주의: 운영 환경에서는 이 부분 제거!
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE user_coupon;
TRUNCATE TABLE order_product;
TRUNCATE TABLE orders;
TRUNCATE TABLE delivery_addresses;
TRUNCATE TABLE cart_products;
TRUNCATE TABLE carts;
TRUNCATE TABLE category_product;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;
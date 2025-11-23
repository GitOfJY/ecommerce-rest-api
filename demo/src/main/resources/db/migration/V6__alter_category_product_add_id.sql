-- V6__alter_category_product_add_id.sql

-- 필요하면 개발 DB라면 더미는 이미 없거나 상관 없음
-- TRUNCATE는 선택 사항 (없어도 DDL은 됨)
-- TRUNCATE TABLE category_product;

-- 1) 먼저 FK들 드롭
ALTER TABLE category_product
    DROP FOREIGN KEY fk_cp_category;

ALTER TABLE category_product
    DROP FOREIGN KEY fk_cp_product;

-- 2) 기존 PRIMARY KEY 제거 (이제 FK에서 안 쓰이니까 드롭 가능)
ALTER TABLE category_product
    DROP PRIMARY KEY;

-- 3) id 컬럼 추가 + PK 지정
ALTER TABLE category_product
    ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

-- 4) (category_id, product_id) 중복 방지 UNIQUE 제약 추가
ALTER TABLE category_product
    ADD CONSTRAINT uk_cp_category_product UNIQUE (category_id, product_id);

-- 5) FK 다시 생성
ALTER TABLE category_product
    ADD CONSTRAINT fk_cp_category FOREIGN KEY (category_id) REFERENCES category(id),
    ADD CONSTRAINT fk_cp_product  FOREIGN KEY (product_id)  REFERENCES product(id);

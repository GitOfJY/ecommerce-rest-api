-- 1) 먼저 FK들 드롭 (참조 중이면 컬럼 수정 불가)
ALTER TABLE category          DROP FOREIGN KEY fk_category_parent;
ALTER TABLE category_product  DROP FOREIGN KEY fk_cp_category;
ALTER TABLE category_product  DROP FOREIGN KEY fk_cp_product;

-- 2) PK 컬럼에 AUTO_INCREMENT 부여
ALTER TABLE category MODIFY id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE product  MODIFY id BIGINT NOT NULL AUTO_INCREMENT;

-- 3) FK 재생성 (이름 동일하게 복구)
ALTER TABLE category
    ADD CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_id) REFERENCES category(id) ON DELETE SET NULL;

ALTER TABLE category_product
    ADD CONSTRAINT fk_cp_category FOREIGN KEY (category_id) REFERENCES category(id),
    ADD CONSTRAINT fk_cp_product  FOREIGN KEY (product_id)  REFERENCES product(id);

ALTER TABLE users
    MODIFY id BIGINT NOT NULL AUTO_INCREMENT;
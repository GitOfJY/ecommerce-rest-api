-- 1. coupon_templates → coupons 변경
ALTER TABLE coupon_templates RENAME TO coupons;

-- 2. user_coupons의 FK 제약조건 먼저 삭제
ALTER TABLE user_coupons
    DROP FOREIGN KEY fk_user_coupons_templates,
    DROP FOREIGN KEY fk_user_coupons_users;

-- 3. 컬럼명 변경 (FK 삭제 후에 해야 함)
ALTER TABLE user_coupons
    CHANGE COLUMN template_id coupon_id bigint NOT NULL;

-- 4. 테이블명 변경
ALTER TABLE user_coupons RENAME TO coupon_users;

-- 5. FK 제약조건 재생성 (마지막에)
ALTER TABLE coupon_users
    ADD CONSTRAINT fk_coupon_users_coupons
        FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_coupon_users_users
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
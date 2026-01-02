-- 1. 외래키 제약조건 제거
ALTER TABLE user_coupon
    DROP FOREIGN KEY fk_user_coupon_template;

ALTER TABLE user_coupon
    DROP FOREIGN KEY fk_user_coupon_user;

ALTER TABLE grade_coupon_policy
    DROP FOREIGN KEY fk_grade_coupon_template;

ALTER TABLE grade_coupon_policy
    DROP FOREIGN KEY fk_grade_coupon_grade;

-- 2. coupon_template에 컬럼 추가
ALTER TABLE coupon_template
    ADD COLUMN start_date TIMESTAMP NULL COMMENT '쿠폰 사용 시작일' AFTER valid_days,
    ADD COLUMN end_date TIMESTAMP NULL COMMENT '쿠폰 사용 종료일' AFTER start_date,
    ADD COLUMN usage_limit INT NULL COMMENT '총 발행 가능 수량 (NULL이면 무제한)' AFTER end_date,
    ADD COLUMN issue_count INT NOT NULL DEFAULT 0 COMMENT '발행된 수량' AFTER usage_limit,
    ADD COLUMN used_count INT NOT NULL DEFAULT 0 COMMENT '사용된 수량' AFTER issue_count;

-- 3. coupon_template 인덱스 추가
CREATE INDEX idx_coupon_template_dates ON coupon_template(start_date, end_date);

-- 4. user_coupon에 code와 status 컬럼 추가
ALTER TABLE user_coupon
    ADD COLUMN code VARCHAR(50) NOT NULL AFTER template_id,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' AFTER order_id;

-- 5. code에 UNIQUE 제약조건 추가
ALTER TABLE user_coupon
    ADD CONSTRAINT uk_user_coupon_code UNIQUE (code);

-- 6. user_coupon 인덱스 추가
CREATE INDEX idx_user_coupon_status ON user_coupon(status);
CREATE INDEX idx_user_coupon_user_status ON user_coupon(user_id, status);

-- 7. 테이블명 변경
RENAME TABLE user_coupon TO user_coupons;
RENAME TABLE coupon_template TO coupon_templates;
RENAME TABLE grade_coupon_policy TO grade_coupon_policies;

-- 8. 외래키 재생성
ALTER TABLE user_coupons
    ADD CONSTRAINT fk_user_coupons_templates
        FOREIGN KEY (template_id) REFERENCES coupon_templates(id)
            ON DELETE CASCADE;

ALTER TABLE user_coupons
    ADD CONSTRAINT fk_user_coupons_users
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE CASCADE;

ALTER TABLE grade_coupon_policies
    ADD CONSTRAINT fk_grade_coupon_policies_templates
        FOREIGN KEY (coupon_template_id) REFERENCES coupon_templates(id)
            ON DELETE CASCADE;

ALTER TABLE grade_coupon_policies
    ADD CONSTRAINT fk_grade_coupon_policies_grades
        FOREIGN KEY (grade_id) REFERENCES user_grade(id)
            ON DELETE CASCADE;
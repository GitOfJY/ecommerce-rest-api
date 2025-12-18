CREATE TABLE user_grade (
     id BIGINT PRIMARY KEY AUTO_INCREMENT,
     name VARCHAR(20) NOT NULL,           -- BRONZE, SILVER, GOLD, VIP
     min_purchase_amount INT DEFAULT 0,   -- 승급 기준 금액
     point_rate DECIMAL(3,2) DEFAULT 0,   -- 적립률 (0.01 = 1%)
     discount_rate DECIMAL(3,2) DEFAULT 0,
     free_shipping_threshold INT,          -- 무료배송 기준
     description VARCHAR(200),
     sort_order INT,                       -- 정렬순서
     created_at TIMESTAMP,
     updated_at TIMESTAMP
);

-- users 테이블에 FK 추가
ALTER TABLE users ADD COLUMN grade_id BIGINT DEFAULT 1;
ALTER TABLE users ADD COLUMN total_purchase_amount INT DEFAULT 0;
ALTER TABLE users ADD FOREIGN KEY (grade_id) REFERENCES member_grade(id);

-- 등급별 자동 발행 쿠폰 설정
CREATE TABLE grade_coupon_policy (
    id BIGINT PRIMARY KEY,
    grade_id BIGINT NOT NULL,
    coupon_template_id BIGINT NOT NULL,  -- 어떤 쿠폰을
    issue_type VARCHAR(20),               -- SIGNUP, MONTHLY, GRADE_UP
    FOREIGN KEY (grade_id) REFERENCES member_grade(grade_id)
);

-- 쿠폰 템플릿 (쿠폰 원본)
CREATE TABLE coupon_template (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100),
    discount_type VARCHAR(20),  -- PERCENT, FIXED
    discount_value INT,
    min_order_amount INT,
    max_discount_amount INT,
    valid_days INT              -- 발급 후 유효기간
);

-- 발급된 쿠폰 (사용자별)
CREATE TABLE user_coupon (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    issued_at TIMESTAMP,
    expires_at TIMESTAMP,
    used_at TIMESTAMP,
    order_id BIGINT             -- 사용된 주문
);

- UserGrade 데이터
INSERT INTO user_grade (id, name, min_purchase_amount, point_rate, discount_rate, free_shipping_threshold, sort_order, description) VALUES
(1, 'BRONZE', 0, 0.01, 0, 50000, 1, '신규 회원 기본 등급'),
(2, 'SILVER', 100000, 0.02, 0.01, 30000, 2, '10만원 이상 구매 시 승급'),
(3, 'GOLD', 300000, 0.03, 0.03, 20000, 3, '30만원 이상 구매 시 승급'),
(4, 'VIP', 500000, 0.05, 0.05, 0, 4, '50만원 이상 구매 시 승급, 무료배송');

-- AUTO_INCREMENT 추가하기 (JPA 없이 직접 SQL로 INSERT 할 때 문제발생)
--ALTER TABLE product MODIFY COLUMN id BIGINT AUTO_INCREMENT;
--ALTER TABLE member_grade MODIFY COLUMN id BIGINT AUTO_INCREMENT;
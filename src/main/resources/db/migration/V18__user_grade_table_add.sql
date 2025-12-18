-- V18__user_grade_table_add.sql

-- 1. user_grade 테이블 생성
CREATE TABLE IF NOT EXISTS user_grade (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          name VARCHAR(20) NOT NULL UNIQUE,
                                          min_purchase_amount INT DEFAULT 0,
                                          point_rate DECIMAL(3,2) DEFAULT 0,
                                          discount_rate DECIMAL(3,2) DEFAULT 0,
                                          free_shipping_threshold INT,
                                          description VARCHAR(200),
                                          sort_order INT,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. ✅ INSERT를 먼저! (FK 추가 전에!)
INSERT INTO user_grade (id, name, min_purchase_amount, point_rate, discount_rate, free_shipping_threshold, sort_order, description) VALUES
                                                                                                                                        (1, 'BRONZE', 0, 0.01, 0.00, 50000, 1, '신규 회원 기본 등급'),
                                                                                                                                        (2, 'SILVER', 100000, 0.02, 0.01, 30000, 2, '10만원 이상 구매 시 승급'),
                                                                                                                                        (3, 'GOLD', 300000, 0.03, 0.03, 20000, 3, '30만원 이상 구매 시 승급'),
                                                                                                                                        (4, 'VIP', 500000, 0.05, 0.05, 0, 4, '50만원 이상 구매 시 승급, 무료배송');

-- 3. users 테이블에 컬럼 추가
ALTER TABLE users ADD COLUMN grade_id BIGINT DEFAULT 1;
ALTER TABLE users ADD COLUMN total_purchase_amount INT DEFAULT 0;

-- 4. FK 추가 (이제 user_grade에 id=1이 있으므로 성공!)
ALTER TABLE users
    ADD CONSTRAINT fk_user_grade
        FOREIGN KEY (grade_id) REFERENCES user_grade(id);

-- 5. coupon_template 테이블
CREATE TABLE IF NOT EXISTS coupon_template (
                                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                               name VARCHAR(100) NOT NULL,
                                               discount_type VARCHAR(20) NOT NULL,
                                               discount_value INT NOT NULL,
                                               min_order_amount INT DEFAULT 0,
                                               max_discount_amount INT,
                                               valid_days INT NOT NULL,
                                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 6. grade_coupon_policy 테이블
CREATE TABLE IF NOT EXISTS grade_coupon_policy (
                                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                   grade_id BIGINT NOT NULL,
                                                   coupon_template_id BIGINT NOT NULL,
                                                   issue_type VARCHAR(20) NOT NULL,
                                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                   CONSTRAINT fk_grade_coupon_grade
                                                       FOREIGN KEY (grade_id) REFERENCES user_grade(id) ON DELETE CASCADE,
                                                   CONSTRAINT fk_grade_coupon_template
                                                       FOREIGN KEY (coupon_template_id) REFERENCES coupon_template(id) ON DELETE CASCADE
);

-- 7. user_coupon 테이블
CREATE TABLE IF NOT EXISTS user_coupon (
                                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                           user_id BIGINT NOT NULL,
                                           template_id BIGINT NOT NULL,
                                           issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           expires_at TIMESTAMP NOT NULL,
                                           used_at TIMESTAMP NULL,
                                           order_id BIGINT NULL,
                                           CONSTRAINT fk_user_coupon_user
                                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                           CONSTRAINT fk_user_coupon_template
                                               FOREIGN KEY (template_id) REFERENCES coupon_template(id) ON DELETE CASCADE
);

-- 8. 인덱스
CREATE INDEX idx_user_grade ON users(grade_id);
CREATE INDEX idx_user_coupon_user ON user_coupon(user_id);
CREATE INDEX idx_user_coupon_expires ON user_coupon(expires_at);
CREATE INDEX idx_user_coupon_used ON user_coupon(used_at);
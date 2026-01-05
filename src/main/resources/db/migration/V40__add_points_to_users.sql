ALTER TABLE users
    ADD COLUMN points INT NOT NULL DEFAULT 0 COMMENT '보유 적립금';

-- 적립금 내역 테이블
CREATE TABLE point_histories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    order_id BIGINT NULL,

    -- 적립금 변동 정보
    point_type VARCHAR(20) NOT NULL COMMENT '적립금 타입 (EARN: 적립, USE: 사용, CANCEL: 취소)',
    amount INT NOT NULL COMMENT '변동 금액 (적립: +, 사용: -)',
    balance_after INT NOT NULL COMMENT '변동 후 잔액',

    -- 상세 정보
    description VARCHAR(255) NOT NULL COMMENT '변동 사유',
    expires_at TIMESTAMP NULL COMMENT '적립금 만료일 (적립 시에만)',

    -- 메타 정보
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 외래키
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
) COMMENT='적립금 내역';
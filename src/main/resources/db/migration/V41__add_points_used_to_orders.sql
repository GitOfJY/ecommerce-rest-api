-- orders 테이블에 적립금 사용 금액 컬럼 추가
ALTER TABLE orders
    ADD COLUMN points_used INT NOT NULL DEFAULT 0 COMMENT '사용한 적립금';
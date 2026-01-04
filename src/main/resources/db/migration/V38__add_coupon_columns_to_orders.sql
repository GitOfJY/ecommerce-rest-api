-- orders 테이블에 쿠폰 관련 컬럼 추가
ALTER TABLE orders
ADD COLUMN coupon_user_id BIGINT,
ADD COLUMN coupon_discount DECIMAL(13, 2) DEFAULT 0.00;

-- 인덱스 추가 (성능 최적화)
CREATE INDEX idx_orders_coupon_user_id ON orders(coupon_user_id);
ALTER TABLE products
    ADD COLUMN external_product_id VARCHAR(100) NULL COMMENT '외부 시스템 상품 ID',
    ADD COLUMN source VARCHAR(20) NOT NULL DEFAULT 'INTERNAL' COMMENT '상품 출처 (INTERNAL/EXTERNAL)',
    ADD COLUMN last_synced_at TIMESTAMP NULL COMMENT '마지막 외부 동기화 시점',
    ADD COLUMN is_orderable BOOLEAN NOT NULL DEFAULT TRUE COMMENT '주문 가능 여부';

-- 외부 상품 ID에 유니크 제약조건 추가
ALTER TABLE products
    ADD CONSTRAINT uk_external_product_id UNIQUE (external_product_id);

-- 조회 성능을 위한 인덱스
CREATE INDEX idx_product_source ON products (source);
CREATE INDEX idx_product_orderable ON products (is_orderable);

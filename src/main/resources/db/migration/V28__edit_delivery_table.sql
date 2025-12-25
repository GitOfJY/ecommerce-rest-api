-- delivery_addresses 테이블에 is_temporary 컬럼 추가
ALTER TABLE delivery_addresses
    ADD COLUMN is_temporary BOOLEAN DEFAULT FALSE NOT NULL;

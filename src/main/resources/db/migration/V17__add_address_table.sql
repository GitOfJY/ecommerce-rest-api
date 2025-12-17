-- 1. addresses 테이블 신규 생성
# CREATE TABLE addresses (
#                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
#                            zip_code VARCHAR(10),
#                            city VARCHAR(50),
#                            street VARCHAR(100),
#                            detail VARCHAR(100),
#                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
# );
#
# -- 2. delivery_addresses 테이블 수정
# ALTER TABLE delivery_addresses ADD COLUMN address_id BIGINT;
# ALTER TABLE delivery_addresses ADD COLUMN alias VARCHAR(50);

-- 3. 기존 delivery_addresses.address 데이터 이관
# INSERT INTO addresses (street, created_at)
# SELECT address, created_at FROM delivery_addresses WHERE address IS NOT NULL;
#
# UPDATE delivery_addresses da
#     JOIN addresses a ON a.street = da.address
#     SET da.address_id = a.id;
#
# ALTER TABLE delivery_addresses DROP COLUMN address;
#
# ALTER TABLE delivery_addresses
#     ADD CONSTRAINT fk_delivery_address_address
#         FOREIGN KEY (address_id) REFERENCES addresses(id);

# -- 4. 기존 users.address 데이터 이관
# INSERT INTO addresses (street, created_at)
# SELECT address, NOW() FROM users
# WHERE address IS NOT NULL AND address != ''
# AND address NOT IN (SELECT street FROM addresses WHERE street IS NOT NULL);

INSERT INTO delivery_addresses (user_id, address_id, recipient_name, recipient_phone, recipient_email, alias, is_default, created_at, updated_at)
SELECT
    u.id,
    a.id,
    u.username,
    COALESCE(u.phone, ''),
    u.email,
    '기본주소',
    TRUE,
    NOW(),
    NOW()
FROM users u
         JOIN addresses a ON a.street = u.address
WHERE u.address IS NOT NULL
  AND u.address != ''
AND NOT EXISTS (
    SELECT 1 FROM delivery_addresses da WHERE da.user_id = u.id
);

-- 5. users 테이블에서 address 컬럼 삭제
ALTER TABLE users DROP COLUMN address;

-- 6. 각 유저별 기본 배송지 하나만 설정
UPDATE delivery_addresses d1
SET is_default = FALSE
WHERE is_default = TRUE
  AND id != (
    SELECT MIN(id) FROM (SELECT * FROM delivery_addresses) d2
    WHERE d2.user_id = d1.user_id AND d2.is_default = TRUE
);
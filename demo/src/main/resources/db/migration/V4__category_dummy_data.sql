-- ====== 기존 더미데이터 초기화 ======
DELETE FROM category;

-- ====== 최상위 카테고리 ======
INSERT INTO category (id, name, description, parent_id) VALUES
        (1, '여성의류', '여성 패션 카테고리', NULL),
        (3, '잡화', '신발/가방/모자/악세서리 등', NULL);

-- ====== 여성의류 하위 ======
INSERT INTO category (id, name, description, parent_id) VALUES
        (10, '티셔츠', '여성 티셔츠 카테고리', 1),
        (11, '니트/스웨터', '여성 니트 및 스웨터', 1),
        (12, '원피스', '여성 원피스', 1),
        (13, '아우터', '여성 아우터', 1),
        (14, '바지', '여성 팬츠', 1);

-- ====== 잡화 하위 ======
INSERT INTO category (id, name, description, parent_id) VALUES
        (30, '신발', '신발 전체 카테고리', 3),
        (31, '가방', '백/크로스백/토트백', 3),
        (32, '모자', '모자/캡/비니', 3),
        (33, '악세서리', '귀걸이/목걸이/반지 등 악세서리', 3);

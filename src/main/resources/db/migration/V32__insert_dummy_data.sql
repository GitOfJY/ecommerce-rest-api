-- 상품 더미 데이터 (30개)
INSERT INTO products (name, description, price, created_at, updated_at, average_rating, review_count) VALUES
-- 상의 (니트/스웨터)
('베이직 라운드 니트', '부드러운 촉감의 데일리 라운드 니트입니다.', 39900, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 5 DAY, 4.5, 23),
('캐시미어 혼방 터틀넥', '고급스러운 캐시미어 혼방 소재의 터틀넥입니다.', 89900, NOW() - INTERVAL 28 DAY, NOW() - INTERVAL 3 DAY, 4.8, 45),
('오버핏 크루넥 니트', '여유있는 핏의 크루넥 니트입니다.', 45900, NOW() - INTERVAL 25 DAY, NOW() - INTERVAL 2 DAY, 4.3, 18),

-- 상의 (맨투맨/스웨터)
('베이직 기모 맨투맨', '따뜻한 기모 안감의 기본 맨투맨입니다.', 29900, NOW() - INTERVAL 27 DAY, NOW() - INTERVAL 4 DAY, 4.6, 67),
('오버핏 후드집업', '편안한 오버핏의 후드 집업입니다.', 59900, NOW() - INTERVAL 26 DAY, NOW() - INTERVAL 6 DAY, 4.4, 34),

-- 상의 (반소매 티셔츠)
('베이직 라운드 반팔티', '100% 면 소재의 베이직 반팔 티셔츠입니다.', 19900, NOW() - INTERVAL 24 DAY, NOW() - INTERVAL 1 DAY, 4.7, 89),
('오버핏 반팔 티셔츠', '여유있는 핏의 반팔 티셔츠입니다.', 24900, NOW() - INTERVAL 23 DAY, NOW() - INTERVAL 2 DAY, 4.5, 56),
('스트라이프 반팔티', '깔끔한 스트라이프 패턴의 반팔티입니다.', 27900, NOW() - INTERVAL 22 DAY, NOW() - INTERVAL 3 DAY, 4.2, 32),

-- 바지 (데님 팬츠)
('스트레이트 청바지', '편안한 핏의 스트레이트 청바지입니다.', 49900, NOW() - INTERVAL 21 DAY, NOW() - INTERVAL 4 DAY, 4.6, 78),
('슬림핏 블랙진', '슬림한 핏의 블랙 진입니다.', 54900, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 5 DAY, 4.4, 45),
('와이드 데님 팬츠', '편안한 와이드 핏의 데님 팬츠입니다.', 59900, NOW() - INTERVAL 19 DAY, NOW() - INTERVAL 6 DAY, 4.3, 34),

-- 바지 (코튼 팬츠)
('베이직 치노 팬츠', '데일리로 입기 좋은 치노 팬츠입니다.', 39900, NOW() - INTERVAL 18 DAY, NOW() - INTERVAL 7 DAY, 4.5, 67),
('와이드 코튼 팬츠', '여유있는 와이드 핏의 코튼 팬츠입니다.', 44900, NOW() - INTERVAL 17 DAY, NOW() - INTERVAL 8 DAY, 4.7, 52),

-- 원피스/스커트 (미디원피스)
('플레어 미디 원피스', '여성스러운 플레어 핏의 미디 원피스입니다.', 69900, NOW() - INTERVAL 16 DAY, NOW() - INTERVAL 9 DAY, 4.8, 43),
('니트 미디 원피스', '따뜻한 니트 소재의 미디 원피스입니다.', 79900, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 10 DAY, 4.6, 38),

-- 원피스/스커트 (미디스커트)
('플리츠 미디스커트', '우아한 플리츠 디테일의 미디스커트입니다.', 49900, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 11 DAY, 4.4, 29),
('A라인 미디스커트', '편안한 A라인 핏의 미디스커트입니다.', 39900, NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 12 DAY, 4.5, 36),

-- 가방 (숄더백)
('미니 크로스백', '실용적인 미니 사이즈의 크로스백입니다.', 29900, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 13 DAY, 4.3, 54),
('가죽 숄더백', '고급스러운 가죽 소재의 숄더백입니다.', 89900, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 14 DAY, 4.7, 67),

-- 가방 (토트백)
('캔버스 에코백', '튼튼한 캔버스 소재의 에코백입니다.', 19900, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 15 DAY, 4.6, 123),
('레더 토트백', '실용적인 레더 토트백입니다.', 119900, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 16 DAY, 4.8, 89),

-- 신발 (스니커즈)
('캔버스 스니커즈', '편안한 캔버스 소재의 스니커즈입니다.', 39900, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 17 DAY, 4.4, 98),
('레더 화이트 스니커즈', '깔끔한 화이트 레더 스니커즈입니다.', 79900, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 18 DAY, 4.7, 112),
('런닝 스니커즈', '운동하기 좋은 런닝 스니커즈입니다.', 89900, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 19 DAY, 4.5, 76),

-- 신발 (부츠/워커)
('앵클 첼시 부츠', '심플한 디자인의 앵클 첼시 부츠입니다.', 99900, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 20 DAY, 4.6, 54),
('워커 부츠', '튼튼한 워커 부츠입니다.', 129900, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 21 DAY, 4.3, 43),

-- 패션소품 (모자)
('베이직 볼캡', '데일리로 쓰기 좋은 베이직 볼캡입니다.', 19900, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 22 DAY, 4.5, 67),
('니트 비니', '따뜻한 니트 비니입니다.', 15900, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 23 DAY, 4.7, 89),

-- 패션소품 (액세서리)
('실버 체인 목걸이', '심플한 디자인의 실버 체인 목걸이입니다.', 29900, NOW() - INTERVAL 1 DAY, NOW(), 4.4, 45),
('레더 팔찌', '캐주얼한 레더 팔찌입니다.', 24900, NOW(), NOW(), 4.6, 38);

-- ✅ 상품 ID를 변수로 저장 (MySQL 8.0+)
SET @product_start_id = LAST_INSERT_ID();

-- 카테고리-상품 연결 (category_products 테이블명 확인!)
-- 동적으로 계산된 product_id 사용
INSERT INTO category_product (category_id, product_id) VALUES
-- 니트/스웨터 (7번 카테고리)
(7, @product_start_id + 0),  -- 상품 1
(7, @product_start_id + 1),  -- 상품 2
(7, @product_start_id + 2),  -- 상품 3

-- 맨투맨/스웨터 (8번 카테고리)
(8, @product_start_id + 3),  -- 상품 4
(8, @product_start_id + 4),  -- 상품 5

-- 반소매 티셔츠 (12번 카테고리)
(12, @product_start_id + 5),  -- 상품 6
(12, @product_start_id + 6),  -- 상품 7
(12, @product_start_id + 7),  -- 상품 8

-- 데님 팬츠 (18번 카테고리)
(18, @product_start_id + 8),   -- 상품 9
(18, @product_start_id + 9),   -- 상품 10
(18, @product_start_id + 10),  -- 상품 11

-- 코튼 팬츠 (17번 카테고리)
(17, @product_start_id + 11),  -- 상품 12
(17, @product_start_id + 12),  -- 상품 13

-- 미디원피스 (27번 카테고리)
(27, @product_start_id + 13),  -- 상품 14
(27, @product_start_id + 14),  -- 상품 15

-- 미디스커트 (24번 카테고리)
(24, @product_start_id + 15),  -- 상품 16
(24, @product_start_id + 16),  -- 상품 17

-- 숄더백 (38번 카테고리)
(38, @product_start_id + 17),  -- 상품 18
(38, @product_start_id + 18),  -- 상품 19

-- 토트백 (40번 카테고리)
(40, @product_start_id + 19),  -- 상품 20
(40, @product_start_id + 20),  -- 상품 21

-- 스니커즈 (50번 카테고리)
(50, @product_start_id + 21),  -- 상품 22
(50, @product_start_id + 22),  -- 상품 23
(50, @product_start_id + 23),  -- 상품 24

-- 부츠/워커 (52번 카테고리)
(52, @product_start_id + 24),  -- 상품 25
(52, @product_start_id + 25),  -- 상품 26

-- 모자 (33번 카테고리)
(33, @product_start_id + 26),  -- 상품 27
(33, @product_start_id + 27),  -- 상품 28

-- 액세서리 (30번 카테고리)
(30, @product_start_id + 28),  -- 상품 29
(30, @product_start_id + 29);  -- 상품 30

-- 상품 옵션 데이터
INSERT INTO product_options (product_id, color, size, stock, additional_price) VALUES
-- 상품 1: 베이직 라운드 니트
(@product_start_id + 0, '블랙', 'S', 10, 0),
(@product_start_id + 0, '블랙', 'M', 15, 0),
(@product_start_id + 0, '블랙', 'L', 12, 0),
(@product_start_id + 0, '화이트', 'S', 8, 0),
(@product_start_id + 0, '화이트', 'M', 20, 0),
(@product_start_id + 0, '화이트', 'L', 15, 0),

-- 상품 2: 캐시미어 혼방 터틀넥
(@product_start_id + 1, '네이비', 'M', 5, 0),
(@product_start_id + 1, '네이비', 'L', 8, 0),
(@product_start_id + 1, '그레이', 'M', 6, 0),
(@product_start_id + 1, '그레이', 'L', 10, 0),

-- 상품 6: 베이직 라운드 반팔티
(@product_start_id + 5, '화이트', 'S', 30, 0),
(@product_start_id + 5, '화이트', 'M', 50, 0),
(@product_start_id + 5, '화이트', 'L', 40, 0),
(@product_start_id + 5, '블랙', 'S', 25, 0),
(@product_start_id + 5, '블랙', 'M', 45, 0),
(@product_start_id + 5, '블랙', 'L', 35, 0),

-- 상품 9: 스트레이트 청바지
(@product_start_id + 8, '인디고', '28', 10, 0),
(@product_start_id + 8, '인디고', '29', 15, 0),
(@product_start_id + 8, '인디고', '30', 20, 0),
(@product_start_id + 8, '인디고', '31', 18, 0),
(@product_start_id + 8, '인디고', '32', 12, 0),

-- 상품 22: 캔버스 스니커즈
(@product_start_id + 21, '화이트', '250', 15, 0),
(@product_start_id + 21, '화이트', '255', 20, 0),
(@product_start_id + 21, '화이트', '260', 25, 0),
(@product_start_id + 21, '화이트', '265', 20, 0),
(@product_start_id + 21, '화이트', '270', 15, 0),
(@product_start_id + 21, '블랙', '250', 10, 0),
(@product_start_id + 21, '블랙', '255', 15, 0),
(@product_start_id + 21, '블랙', '260', 20, 0),
(@product_start_id + 21, '블랙', '265', 15, 0),
(@product_start_id + 21, '블랙', '270', 10, 0);
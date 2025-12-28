ALTER TABLE products
    ADD COLUMN average_rating DECIMAL(3, 2) DEFAULT 0.00,
    ADD COLUMN review_count INT DEFAULT 0;

ALTER TABLE reviews DROP COLUMN title;
ALTER TABLE reviews DROP COLUMN helpful_count;

ALTER TABLE reviews
    ADD COLUMN size_rating INT COMMENT '사이즈 평가 (1:매우작음, 2:작음, 3:보통, 4:큼, 5:매우큼)',
    ADD COLUMN color_rating INT COMMENT '색감 평가 (1:매우어두움, 2:어두움, 3:보통, 4:밝음, 5:매우밝음)',
    ADD COLUMN thickness_rating INT COMMENT '두께감 평가 (1:매우얇음, 2:얇음, 3:보통, 4:두꺼움, 5:매우두꺼움)';

ALTER TABLE reviews
    MODIFY COLUMN content TEXT NOT NULL COMMENT '리뷰 내용 (20자 이상 500자 이하)';


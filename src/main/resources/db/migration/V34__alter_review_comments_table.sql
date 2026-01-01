ALTER TABLE review_comments
    ADD COLUMN parent_comment_id BIGINT NULL,
    ADD FOREIGN KEY (parent_comment_id) REFERENCES review_comments(id) ON DELETE CASCADE;
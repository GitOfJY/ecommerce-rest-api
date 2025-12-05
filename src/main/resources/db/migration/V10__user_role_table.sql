CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,   -- ROLE_USER, ROLE_ADMIN 등
    description VARCHAR(255)            -- 역할 설명
);

INSERT INTO roles (id, name, description) VALUES
                (1, 'ROLE_USER', '일반회원'),
                (2, 'ROLE_ADMIN', '관리자'),
                (3, 'ROLE_GUEST', '비회원');

ALTER TABLE users
    ADD COLUMN role_id BIGINT NOT NULL DEFAULT 1,  -- 기본은 일반 회원 ROLE_USER
    ADD CONSTRAINT fk_users_role
        FOREIGN KEY (role_id) REFERENCES roles(id);

INSERT INTO users (username, email, password_hash, address, role_id) VALUES
    ('admin', 'admin@test.com', 'admin1234', 'Seoul', 2);

CREATE TABLE refund (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        status VARCHAR(20) NOT NULL,
        user_id BIGINT NOT NULL,
        order_id BIGINT NOT NULL,
        reason VARCHAR(500) NOT NULL,
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        processed_at DATETIME NULL,
        processed_by BIGINT NULL,
        refund_amount DECIMAL(15, 2) NOT NULL,

        CONSTRAINT fk_refund_user FOREIGN KEY (user_id) REFERENCES users(id),
        CONSTRAINT fk_refund_order FOREIGN KEY (order_id) REFERENCES orders(id),
        CONSTRAINT fk_refund_processed_by FOREIGN KEY (processed_by) REFERENCES users(id)
);

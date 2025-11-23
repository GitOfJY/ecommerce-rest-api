-- 필수 입력 필드: name, description, price, stock, category_id
CREATE TABLE product (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    price DECIMAL(15, 2) NOT NULL,
    stock INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 필수 입력 필드: name, description, parent_id
CREATE TABLE category (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    parent_id BIGINT,

    CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_id)
            REFERENCES category(id)
            ON DELETE SET NULL
);


CREATE TABLE category_product (
    category_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (category_id, product_id),
    CONSTRAINT fk_cp_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT fk_cp_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

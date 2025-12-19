ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE users ADD COLUMN withdrawn_at TIMESTAMP;

-- TRUNCATE TABLE
--     order_products,
--     orders,
--     delivery_addresses,
--     addresses,
--     users,
--     user_grades,
--     roles
-- RESTART IDENTITY CASCADE;
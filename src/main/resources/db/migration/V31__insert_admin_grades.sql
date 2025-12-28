INSERT INTO user_grade (id, name, discount_rate, min_purchase_amount)
VALUES (5, 'ADMIN', 0.00, 0.00)
ON DUPLICATE KEY UPDATE name=name;
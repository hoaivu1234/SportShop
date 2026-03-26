-- Add columns missing from the initial schema
ALTER TABLE coupons
    ADD COLUMN max_discount_value DECIMAL(10, 2)   NULL        AFTER min_order_value,
    ADD COLUMN usage_limit        INT               NULL        AFTER max_discount_value,
    ADD COLUMN used_count         INT    NOT NULL   DEFAULT 0   AFTER usage_limit,
    ADD COLUMN created_at         TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at         TIMESTAMP         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Ensure status has a safe default
ALTER TABLE coupons
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

CREATE INDEX idx_coupons_code   ON coupons (code);
CREATE INDEX idx_coupons_status ON coupons (status);

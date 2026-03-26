-- Add updated_at column to reviews table (was missing in initial schema)
ALTER TABLE reviews
    ADD COLUMN updated_at DATETIME NULL AFTER created_at;

-- Ensure unique constraint exists (one review per user per product)
-- (entity-level @UniqueConstraint handles DDL in dev; this migration is the source of truth for prod)
ALTER TABLE reviews
    ADD CONSTRAINT uq_reviews_user_product UNIQUE (user_id, product_id);

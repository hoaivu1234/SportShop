CREATE TABLE wishlist_items (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    variant_id  BIGINT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_wishlist_user_product UNIQUE (user_id, product_id),
    CONSTRAINT fk_wishlist_user    FOREIGN KEY (user_id)    REFERENCES users(id)             ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_product FOREIGN KEY (product_id) REFERENCES products(id)          ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id)  ON DELETE SET NULL
);

CREATE INDEX idx_wishlist_user_id    ON wishlist_items (user_id);
CREATE INDEX idx_wishlist_product_id ON wishlist_items (product_id);

package com.sport.ecommerce.modules.cart.entity;

import com.sport.ecommerce.modules.product.entity.variant.ProductVariant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private Integer quantity;

    /** Price locked in at the moment the item was added to cart. */
    @Column(name = "price_snapshot", precision = 12, scale = 2)
    private BigDecimal priceSnapshot;

    @CreationTimestamp
    @Column(name = "added_at")
    private LocalDateTime addedAt;
}

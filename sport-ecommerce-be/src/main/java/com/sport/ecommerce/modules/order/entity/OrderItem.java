package com.sport.ecommerce.modules.order.entity;

import com.sport.ecommerce.modules.product.entity.variant.ProductVariant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Soft-link to the variant — kept nullable so that an order history
     * survives even if the variant is later deleted from the catalogue.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    // ── Price / quantity snapshots (immutable after order is placed) ─────────

    @Column(length = 255, nullable = false)
    private String productName;

    /** Unit price captured at checkout time. */
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    /** price × quantity */
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal;

    // ── Variant attribute snapshots ──────────────────────────────────────────

    @Column(length = 100)
    private String variantSku;

    @Column(length = 50)
    private String variantSize;

    @Column(length = 50)
    private String variantColor;

    @Column(length = 500)
    private String productImageUrl;
}

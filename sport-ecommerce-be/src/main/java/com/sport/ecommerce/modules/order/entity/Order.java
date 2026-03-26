package com.sport.ecommerce.modules.order.entity;

import com.sport.ecommerce.modules.order.enums.OrderStatus;
import com.sport.ecommerce.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(name = "idx_orders_user_id",     columnList = "user_id"),
        @Index(name = "idx_orders_order_number", columnList = "order_number", unique = true),
        @Index(name = "idx_orders_status",       columnList = "status"),
        @Index(name = "idx_orders_created_at",   columnList = "created_at"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100, nullable = false, unique = true)
    private String orderNumber;

    // ── Line-item aggregate ──────────────────────────────────────────────────

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // ── Money ────────────────────────────────────────────────────────────────

    /** Sum of (item.price × item.quantity) before any discount. */
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal;

    /** Amount subtracted by coupons / promotions (0 if none). */
    @Column(precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** Flat shipping fee added to the order. */
    @Column(precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    /** subtotal - discountAmount + shippingFee */
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    // ── Status ───────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(length = 30)
    private String paymentStatus;

    // ── Shipping address snapshot ─────────────────────────────────────────────

    @Convert(converter = ShippingAddressConverter.class)
    @Column(columnDefinition = "TEXT", nullable = false)
    private ShippingAddress shippingAddress;

    // ── Metadata ─────────────────────────────────────────────────────────────

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ── Helpers ──────────────────────────────────────────────────────────────

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}

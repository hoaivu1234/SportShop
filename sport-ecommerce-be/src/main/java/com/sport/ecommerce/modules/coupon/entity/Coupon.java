package com.sport.ecommerce.modules.coupon.entity;

import com.sport.ecommerce.modules.coupon.enums.CouponStatus;
import com.sport.ecommerce.modules.coupon.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "coupons",
    indexes = {
        @Index(name = "idx_coupons_code",   columnList = "code"),
        @Index(name = "idx_coupons_status", columnList = "status")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    /** Minimum cart total required to use this coupon. */
    @Column(precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    /** Maximum discount that can be applied (caps PERCENT discounts). */
    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscountValue;

    /** null = unlimited usage. */
    private Integer usageLimit;

    @Column(nullable = false)
    @Builder.Default
    private int usedCount = 0;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CouponStatus status = CouponStatus.ACTIVE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

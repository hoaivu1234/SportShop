package com.sport.ecommerce.modules.coupon.dto.response;

import com.sport.ecommerce.modules.coupon.enums.CouponStatus;
import com.sport.ecommerce.modules.coupon.enums.DiscountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CouponResponse {

    private Long id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;
    private Integer usageLimit;
    private int usedCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private CouponStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

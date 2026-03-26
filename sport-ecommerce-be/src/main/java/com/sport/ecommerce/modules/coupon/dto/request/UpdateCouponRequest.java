package com.sport.ecommerce.modules.coupon.dto.request;

import com.sport.ecommerce.modules.coupon.enums.CouponStatus;
import com.sport.ecommerce.modules.coupon.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateCouponRequest {

    private DiscountType discountType;

    @DecimalMin(value = "0.01", message = "Discount value must be > 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.00", message = "Min order value must be >= 0")
    private BigDecimal minOrderValue;

    @DecimalMin(value = "0.01", message = "Max discount value must be > 0")
    private BigDecimal maxDiscountValue;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private CouponStatus status;
}

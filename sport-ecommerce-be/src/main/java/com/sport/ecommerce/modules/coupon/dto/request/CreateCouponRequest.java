package com.sport.ecommerce.modules.coupon.dto.request;

import com.sport.ecommerce.modules.coupon.enums.CouponStatus;
import com.sport.ecommerce.modules.coupon.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 50, message = "Code must be 3–50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_\\-]+$", message = "Code may only contain letters, digits, hyphens, and underscores")
    private String code;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
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

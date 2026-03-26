package com.sport.ecommerce.modules.coupon.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplyCouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;

    @NotNull(message = "Cart total is required")
    @DecimalMin(value = "0.00", message = "Cart total must be >= 0")
    private BigDecimal cartTotal;
}

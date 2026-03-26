package com.sport.ecommerce.modules.coupon.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ApplyCouponResponse {

    private String couponCode;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String message;
}

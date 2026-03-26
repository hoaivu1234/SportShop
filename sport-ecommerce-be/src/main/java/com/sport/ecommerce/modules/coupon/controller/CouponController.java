package com.sport.ecommerce.modules.coupon.controller;

import com.sport.ecommerce.common.constant.AppConstant;
import com.sport.ecommerce.common.dto.response.ApiResponse;
import com.sport.ecommerce.modules.coupon.dto.request.ApplyCouponRequest;
import com.sport.ecommerce.modules.coupon.dto.response.ApplyCouponResponse;
import com.sport.ecommerce.modules.coupon.dto.response.CouponResponse;
import com.sport.ecommerce.modules.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.API_PREFIX + "/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    /** Look up a coupon by code — lets the frontend show coupon details before applying. */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CouponResponse>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getCouponByCode(code)));
    }

    /**
     * Validate and calculate the discount for a coupon code.
     * Stateless — does NOT increment usedCount; that happens at order placement.
     */
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<ApplyCouponResponse>> applyCoupon(
            @Valid @RequestBody ApplyCouponRequest request) {
        return ResponseEntity.ok(ApiResponse.success(couponService.applyCoupon(request)));
    }
}

package com.sport.ecommerce.modules.coupon.controller;

import com.sport.ecommerce.common.constant.AppConstant;
import com.sport.ecommerce.common.dto.response.ApiResponse;
import com.sport.ecommerce.modules.coupon.dto.request.CreateCouponRequest;
import com.sport.ecommerce.modules.coupon.dto.request.UpdateCouponRequest;
import com.sport.ecommerce.modules.coupon.dto.response.CouponResponse;
import com.sport.ecommerce.modules.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.ADMIN_PREFIX + "/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CouponResponse>>> getCoupons(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getCoupons(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getCouponById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CreateCouponRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(couponService.createCoupon(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCouponRequest request) {
        return ResponseEntity.ok(ApiResponse.success(couponService.updateCoupon(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

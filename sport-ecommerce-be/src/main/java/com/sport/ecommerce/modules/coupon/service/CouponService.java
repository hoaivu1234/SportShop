package com.sport.ecommerce.modules.coupon.service;

import com.sport.ecommerce.modules.coupon.dto.request.ApplyCouponRequest;
import com.sport.ecommerce.modules.coupon.dto.request.CreateCouponRequest;
import com.sport.ecommerce.modules.coupon.dto.request.UpdateCouponRequest;
import com.sport.ecommerce.modules.coupon.dto.response.ApplyCouponResponse;
import com.sport.ecommerce.modules.coupon.dto.response.CouponResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponService {

    Page<CouponResponse> getCoupons(Pageable pageable);

    CouponResponse getCouponById(Long id);

    CouponResponse getCouponByCode(String code);

    CouponResponse createCoupon(CreateCouponRequest request);

    CouponResponse updateCoupon(Long id, UpdateCouponRequest request);

    void deleteCoupon(Long id);

    ApplyCouponResponse applyCoupon(ApplyCouponRequest request);
}

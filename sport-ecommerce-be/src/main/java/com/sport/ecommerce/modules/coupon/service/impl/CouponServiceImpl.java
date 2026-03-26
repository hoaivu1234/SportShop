package com.sport.ecommerce.modules.coupon.service.impl;

import com.sport.ecommerce.exception.custom.BusinessException;
import com.sport.ecommerce.modules.coupon.dto.request.ApplyCouponRequest;
import com.sport.ecommerce.modules.coupon.dto.request.CreateCouponRequest;
import com.sport.ecommerce.modules.coupon.dto.request.UpdateCouponRequest;
import com.sport.ecommerce.modules.coupon.dto.response.ApplyCouponResponse;
import com.sport.ecommerce.modules.coupon.dto.response.CouponResponse;
import com.sport.ecommerce.modules.coupon.entity.Coupon;
import com.sport.ecommerce.modules.coupon.enums.CouponStatus;
import com.sport.ecommerce.modules.coupon.enums.DiscountType;
import com.sport.ecommerce.modules.coupon.repository.CouponRepository;
import com.sport.ecommerce.modules.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    // ── Admin CRUD ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> getCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCouponById(Long id) {
        return toResponse(requireCoupon(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND.value(), "Coupon not found: " + code));
        return toResponse(coupon);
    }

    @Override
    @Transactional
    public CouponResponse createCoupon(CreateCouponRequest request) {
        String upperCode = request.getCode().toUpperCase();

        if (couponRepository.existsByCodeIgnoreCase(upperCode)) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Coupon code already exists: " + upperCode);
        }

        Coupon coupon = Coupon.builder()
                .code(upperCode)
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue())
                .maxDiscountValue(request.getMaxDiscountValue())
                .usageLimit(request.getUsageLimit())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus() != null ? request.getStatus() : CouponStatus.ACTIVE)
                .build();

        Coupon saved = couponRepository.save(coupon);
        log.info("Coupon created: {}", saved.getCode());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(Long id, UpdateCouponRequest request) {
        Coupon coupon = requireCoupon(id);

        if (request.getDiscountType()     != null) coupon.setDiscountType(request.getDiscountType());
        if (request.getDiscountValue()    != null) coupon.setDiscountValue(request.getDiscountValue());
        if (request.getMinOrderValue()    != null) coupon.setMinOrderValue(request.getMinOrderValue());
        if (request.getMaxDiscountValue() != null) coupon.setMaxDiscountValue(request.getMaxDiscountValue());
        if (request.getUsageLimit()       != null) coupon.setUsageLimit(request.getUsageLimit());
        if (request.getStartDate()        != null) coupon.setStartDate(request.getStartDate());
        if (request.getEndDate()          != null) coupon.setEndDate(request.getEndDate());
        if (request.getStatus()           != null) coupon.setStatus(request.getStatus());

        return toResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        requireCoupon(id);
        couponRepository.deleteById(id);
        log.info("Coupon {} deleted", id);
    }

    // ── Apply (stateless validation + calculation) ────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApplyCouponResponse applyCoupon(ApplyCouponRequest request) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(request.getCode())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND.value(), "Coupon not found"));

        validate(coupon, request.getCartTotal());

        BigDecimal discount = calculateDiscount(coupon, request.getCartTotal());
        BigDecimal finalAmount = request.getCartTotal().subtract(discount).max(BigDecimal.ZERO);

        return ApplyCouponResponse.builder()
                .couponCode(coupon.getCode())
                .discountAmount(discount)
                .finalAmount(finalAmount)
                .message("Coupon applied! You save " + discount.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Coupon requireCoupon(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND.value(), "Coupon not found: " + id));
    }

    private void validate(Coupon coupon, BigDecimal cartTotal) {
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "Coupon is " + coupon.getStatus().name().toLowerCase());
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "Coupon is not yet valid");
        }
        if (coupon.getEndDate() != null && now.isAfter(coupon.getEndDate())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "Coupon has expired");
        }
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "Coupon usage limit has been reached");
        }
        if (coupon.getMinOrderValue() != null
                && cartTotal.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "Minimum order value of $" + coupon.getMinOrderValue().setScale(2, RoundingMode.HALF_UP)
                            + " required");
        }
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal cartTotal) {
        BigDecimal discount;

        if (coupon.getDiscountType() == DiscountType.PERCENT) {
            discount = cartTotal
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            // FIXED
            discount = coupon.getDiscountValue().setScale(2, RoundingMode.HALF_UP);
        }

        // Cap to maxDiscountValue
        if (coupon.getMaxDiscountValue() != null) {
            discount = discount.min(coupon.getMaxDiscountValue());
        }

        // Cannot exceed the cart total
        return discount.min(cartTotal);
    }

    private CouponResponse toResponse(Coupon c) {
        return CouponResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .discountType(c.getDiscountType())
                .discountValue(c.getDiscountValue())
                .minOrderValue(c.getMinOrderValue())
                .maxDiscountValue(c.getMaxDiscountValue())
                .usageLimit(c.getUsageLimit())
                .usedCount(c.getUsedCount())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}

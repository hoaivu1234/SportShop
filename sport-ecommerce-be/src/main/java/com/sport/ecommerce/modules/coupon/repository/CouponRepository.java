package com.sport.ecommerce.modules.coupon.repository;

import com.sport.ecommerce.modules.coupon.entity.Coupon;
import com.sport.ecommerce.modules.coupon.enums.CouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    Page<Coupon> findByStatus(CouponStatus status, Pageable pageable);

    /** Atomically increments usedCount to avoid race conditions on high-traffic coupons. */
    @Modifying
    @Query("UPDATE Coupon c SET c.usedCount = c.usedCount + 1 WHERE c.id = :id")
    void incrementUsedCount(@Param("id") Long id);
}

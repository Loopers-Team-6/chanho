package com.loopers.domain.coupon;

import com.loopers.domain.CustomCrudRepository;

import java.util.Optional;

public interface CouponRepository extends CustomCrudRepository<CouponEntity> {
    Optional<CouponEntity> findByOrderId(Long orderId);
}

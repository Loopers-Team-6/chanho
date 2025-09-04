package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, Long> {
    Optional<CouponEntity> findByOrderId(Long orderId);
}

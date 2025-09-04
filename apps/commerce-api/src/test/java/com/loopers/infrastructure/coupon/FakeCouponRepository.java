package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.infrastructure.InMemoryCrudRepository;

import java.util.Optional;

public class FakeCouponRepository extends InMemoryCrudRepository<CouponEntity> implements CouponRepository {

    @Override
    public Optional<CouponEntity> findByOrderId(Long orderId) {
        return map.values().stream()
                .filter(coupon -> coupon.getOrderId() != null && coupon.getOrderId().equals(orderId))
                .findFirst();
    }
}

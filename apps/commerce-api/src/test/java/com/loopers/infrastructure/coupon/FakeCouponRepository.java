package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.infrastructure.InMemoryCrudRepository;

public class FakeCouponRepository extends InMemoryCrudRepository<CouponEntity> implements CouponRepository {
}

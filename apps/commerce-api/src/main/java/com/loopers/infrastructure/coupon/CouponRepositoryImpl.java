package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.infrastructure.AbstractRepositoryImpl;
import org.springframework.stereotype.Component;

@Component
public class CouponRepositoryImpl extends AbstractRepositoryImpl<CouponEntity, CouponJpaRepository> implements CouponRepository {
    public CouponRepositoryImpl(CouponJpaRepository jpaRepository) {
        super(jpaRepository);
    }
}

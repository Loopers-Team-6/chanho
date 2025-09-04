package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.infrastructure.AbstractRepositoryImpl;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CouponRepositoryImpl extends AbstractRepositoryImpl<CouponEntity, CouponJpaRepository> implements CouponRepository {
    public CouponRepositoryImpl(CouponJpaRepository jpaRepository) {
        super(jpaRepository);
    }

    @Override
    public Optional<CouponEntity> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }
}

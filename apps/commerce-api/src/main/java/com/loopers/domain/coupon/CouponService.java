package com.loopers.domain.coupon;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponEntity save(CouponEntity entity) {
        return couponRepository.save(entity);
    }

    public CouponEntity findById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found with id: " + id));
    }
}

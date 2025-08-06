package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponEntity save(CouponEntity entity) {
        return couponRepository.save(entity);
    }

    public CouponEntity findById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "Coupon not found with id: " + id));
    }
}

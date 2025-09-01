package com.loopers.domain.coupon;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponEntity save(CouponEntity entity) {
        return couponRepository.save(entity);
    }

    public CouponEntity findById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Coupon not found with id: " + id));
    }

    @Transactional
    public void restoreCoupon(Long orderId) {
        CouponEntity coupon = couponRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Coupon not found for order id: " + orderId));
        if (!coupon.isUsed()) {
            return;
        }

        coupon.restore();
        couponRepository.save(coupon);
    }
}

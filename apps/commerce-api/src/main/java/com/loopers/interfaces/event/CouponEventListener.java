package com.loopers.interfaces.event;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.event.OrderFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CouponEventListener {

    private final CouponService couponService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderFailedEvent(OrderFailedEvent event) {
        couponService.restoreCoupon(event.orderId());
    }
}

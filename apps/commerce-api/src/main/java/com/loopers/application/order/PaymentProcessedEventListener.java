package com.loopers.application.order;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentProcessedEvent;
import com.loopers.domain.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentProcessedEventListener {

    private final OrderService orderService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentProcessedEvent(PaymentProcessedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("PaymentProcessedEvent cannot be null");
        }
        if (event.paymentStatus() == PaymentStatus.PENDING) {
            return;
        }

        OrderEntity order = orderService.findById(event.orderId());
        switch (event.paymentStatus()) {
            case COMPLETED -> order.complete();
            case FAILED -> order.fail();
        }
        orderService.save(order);
    }
}

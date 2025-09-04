package com.loopers.interfaces.event;

import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentProcessedEventListener {

    private final OrderService orderService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePaymentProcessedEvent(PaymentProcessedEvent event) {
        orderService.updateOrderStatus(event.orderId(), event.status());
    }
}

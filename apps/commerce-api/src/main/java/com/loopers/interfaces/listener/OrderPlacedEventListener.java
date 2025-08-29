package com.loopers.interfaces.listener;

import com.loopers.domain.order.OrderPlacedEvent;
import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderPlacedEventListener {

    private final PaymentService paymentService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        paymentService.requestPayment(event.orderId(), event.paymentMethod(), event.amount());
    }
}

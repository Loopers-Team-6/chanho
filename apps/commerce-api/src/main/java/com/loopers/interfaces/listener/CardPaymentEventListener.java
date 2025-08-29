package com.loopers.interfaces.listener;

import com.loopers.domain.payment.CardPaymentResponseEvent;
import com.loopers.domain.payment.CardPaymentService;
import com.loopers.domain.payment.processor.CardPaymentRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CardPaymentEventListener {

    private final CardPaymentService cardPaymentService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCardPaymentRequest(CardPaymentRequestEvent event) {
        cardPaymentService.processCardPayment(event.paymentId(), event.orderId(), event.amount());
    }

    @Async
    @EventListener
    public void handleCardPaymentRequested(CardPaymentResponseEvent event) {
        cardPaymentService.updatePaymentTransactionInfo(event.paymentId(), event.transactionKey(), event.status());
    }
}

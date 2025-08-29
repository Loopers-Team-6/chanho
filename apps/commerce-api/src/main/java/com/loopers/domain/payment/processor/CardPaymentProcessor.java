package com.loopers.domain.payment.processor;

import com.loopers.domain.payment.CardPaymentEntity;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardPaymentProcessor implements PaymentProcessor {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    public PaymentEntity createPayment(Long orderId, BigDecimal amount) {
        return CardPaymentEntity.create(orderId, amount, null);
    }

    @Override
    public void processPayment(PaymentEntity payment) {
        payment.markAsPending();
        eventPublisher.publishEvent(new CardPaymentRequestEvent(payment.getId(), payment.getOrderId(), payment.getAmount()));
    }
}

package com.loopers.domain.payment.processor;

import com.loopers.domain.payment.CardPaymentEntity;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.infrastructure.payment.PgClient;
import com.loopers.interfaces.api.payment.CardType;
import com.loopers.interfaces.api.payment.PaymentV1Dto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardPaymentProcessor implements PaymentProcessor {

    private final PgClient pgClient;

    @Value("${pg.simulator.callbackUrl}")
    private String callbackUrl;

    @Value("${pg.user-id}")
    private long pgUserId;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    public PaymentEntity createPayment(Long orderId, BigDecimal amount) {
        return CardPaymentEntity.create(orderId, amount, null);
    }

    @Override
    @CircuitBreaker(name = "pg-api", fallbackMethod = "processPaymentFallback")
    @Retry(name = "pg-api")
    public void processPayment(PaymentEntity payment) {
        PaymentV1Dto.Request request = PaymentV1Dto.Request.create(
                payment.getOrderId(),
                CardType.KB,
                "1234-5678-9012-3456",
                payment.getAmount().toPlainString(),
                callbackUrl
        );

        PaymentV1Dto.TransactionResponse response = pgClient.createPaymentRequest(pgUserId, request);
        log.info("Payment request response: {}", response);

        if (response.isSuccess()) {
            CardPaymentEntity cardPayment = (CardPaymentEntity) payment;
            cardPayment.setTransactionKey(response.data().transactionKey());
            return;
        }

        log.info("Payment request failed: {}", response.meta());
        payment.fail();
    }

    private void processPaymentFallback(PaymentEntity payment, Throwable t) {
        log.error("PG 결제 요청 최종 실패. Fallback 실행. orderId: {}, error: {}", payment.getOrderId(), t.getMessage());
        payment.fail();
    }
}

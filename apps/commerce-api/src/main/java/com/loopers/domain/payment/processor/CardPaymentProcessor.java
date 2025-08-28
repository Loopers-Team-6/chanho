package com.loopers.domain.payment.processor;

import com.loopers.domain.payment.*;
import com.loopers.infrastructure.payment.CardType;
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

    private final CardPaymentClient cardPaymentClient;

    @Value("${pg.simulator.callbackUrl}")
    private String callbackUrl;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    public PaymentEntity createPayment(Long orderId, BigDecimal amount) {
        return CardPaymentEntity.create(orderId, amount, null);
    }

    @Override
    @CircuitBreaker(name = "pg-api", fallbackMethod = "fallbackForCircuitBreaker")
    @Retry(name = "pg-api", fallbackMethod = "fallbackForRetry")
    public void processPayment(PaymentEntity payment) {
        // 1. CardPaymentClient를 통해 기존 결제 내역 조회
        cardPaymentClient.findPaymentsByOrderId(payment.getOrderId()).ifPresentOrElse(
                // 2. 기존 결제 내역이 있을 경우
                existingPayment -> {
                    log.info("이미 결제 요청된 주문입니다. orderId: [{}]", payment.getOrderId());
                    CardPaymentClient.TransactionInfo lastTransaction = existingPayment.transactions().getLast();
                    updatePayment(payment, lastTransaction.transactionKey(), lastTransaction.status());
                },
                // 3. 기존 결제 내역이 없을 경우 (신규 요청)
                () -> {
                    log.info("결제 요청 내역이 아직 없습니다. 신규 결제를 요청합니다. orderId: [{}]", payment.getOrderId());
                    var request = new CardPaymentClient.PaymentRequest(
                            payment.getOrderId(),
                            payment.getAmount(),
                            CardType.SAMSUNG,
                            "1234-5678-9012-3456",
                            callbackUrl
                    );

                    cardPaymentClient.requestPayment(request)
                            .ifPresentOrElse(
                                    response -> {
                                        log.info("PG 결제 요청 성공: orderId: {}", payment.getOrderId());
                                        updatePayment(payment, response.transactionKey(), response.status());
                                    },
                                    () -> {
                                        throw new IllegalStateException("PG 결제 요청 실패");
                                    }
                            );
                }
        );
    }

    private void updatePayment(PaymentEntity payment, String transactionKey, PaymentStatus status) {
        CardPaymentEntity cardPayment = (CardPaymentEntity) payment;
        cardPayment.setTransactionKey(transactionKey);

        switch (status) {
            case PENDING -> payment.markAsPending();
            case SUCCESS -> payment.markAsSuccess();
            case FAILED -> payment.markAsFailed();
            case CANCELED -> payment.markAsCanceled();
            default -> throw new IllegalStateException("알 수 없는 결제 상태: " + status);
        }
        log.info("결제 상태 업데이트: orderId: {}, status: {}", payment.getOrderId(), payment.getStatus());
    }

    private void fallbackForRetry(PaymentEntity payment, Throwable t) {
        log.info("이것은 retry fallback입니다. orderId: {}, error: {}", payment.getOrderId(), t.getMessage());
    }

    private void fallbackForCircuitBreaker(PaymentEntity payment, Throwable t) {
        log.info("이것은 circuit breaker fallback입니다. orderId: {}, error: {}", payment.getOrderId(), t.getMessage());
    }

//    private void processPaymentFallback(PaymentEntity payment, Throwable t) {
//        log.error("PG 결제 요청 최종 실패. Fallback 실행. orderId: {}, error: {}", payment.getOrderId(), t.getMessage());
//        payment.fail();
//    }
}

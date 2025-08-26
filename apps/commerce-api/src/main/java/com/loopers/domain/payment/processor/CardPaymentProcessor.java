package com.loopers.domain.payment.processor;

import com.loopers.domain.payment.CardPaymentEntity;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.infrastructure.payment.PgClient;
import com.loopers.interfaces.api.payment.CardType;
import com.loopers.interfaces.api.payment.PaymentV1Dto;
import com.loopers.interfaces.api.payment.TransactionStatus;
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
    @CircuitBreaker(name = "pg-api", fallbackMethod = "fallbackForCircuitBreaker")
    @Retry(name = "pg-api", fallbackMethod = "fallbackForRetry")
    public void processPayment(PaymentEntity payment) {
        PaymentV1Dto.OrderResponse paymentsByOrderId = pgClient.findPaymentsByOrderId(pgUserId, payment.getOrderId());
        if (paymentsByOrderId.meta().isSuccess()) {
            log.info("이미 결제 요청된 주문입니다. orderId: [{}]", payment.getOrderId());
            PaymentV1Dto.TransactionInfo transactionInfo = paymentsByOrderId.data().transactions().getLast();
            updatePayment(payment, transactionInfo.transactionKey(), transactionInfo.status());
            return;
        }

        log.info("결제 요청 내역이 아직 없습니다. 신규 결제를 요청합니다. orderId: [{}]", payment.getOrderId());
        PaymentV1Dto.Request request = PaymentV1Dto.Request.create(
                payment.getOrderId(),
                CardType.KB,
                "1234-5678-9012-3456",
                payment.getAmount().toPlainString(),
                callbackUrl
        );
        PaymentV1Dto.TransactionResponse response = pgClient.createPaymentRequest(pgUserId, request);

        if (response.isFail()) {
            throw new IllegalStateException("PG 결제 요청 실패: " + response.meta().message());
        }

        log.info("PG 결제 요청 성공: orderId: {}", payment.getOrderId());
        updatePayment(payment, response.data().transactionKey(), response.data().status());
    }

    private void updatePayment(PaymentEntity payment, String transactionKey, TransactionStatus status) {
        CardPaymentEntity cardPayment = (CardPaymentEntity) payment;
        cardPayment.setTransactionKey(transactionKey);

        switch (status) {
            case SUCCESS -> payment.markAsSuccess();
            case FAILED -> payment.markAsFailed();
            case PENDING -> payment.markAsPending();
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

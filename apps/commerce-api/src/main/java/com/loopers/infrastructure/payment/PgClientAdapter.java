package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.CardPaymentClient;
import com.loopers.infrastructure.exception.NonRetryableApiCallException;
import com.loopers.infrastructure.exception.RetryableApiCallException;
import com.loopers.infrastructure.payment.dto.PgApiDto;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PgClientAdapter implements CardPaymentClient {

    private final PgClient pgClient;

    @Value("${pg.user-id}")
    private long pgUserId;

    @Override
    @CircuitBreaker(name = "pg-api-request", fallbackMethod = "requestPaymentFallback")
    @Retry(name = "pg-api-request")
    public Optional<TransactionInfo> requestPayment(PaymentRequest request) {
        PgApiDto.Request pgRequest = PgApiDto.Request.of(request);

        try {
            PgApiDto.TransactionResponse response = pgClient.createPaymentRequest(pgUserId, pgRequest);

            if (response.isFail()) {
                return Optional.empty();
            }

            return Optional.of(new TransactionInfo(
                    response.data().transactionKey(),
                    response.data().status().toPaymentStatus()
            ));
        } catch (FeignException e) {
            log.error("PG 결제 요청 FeignException 발생: status={}, message={}", e.status(), e.getMessage());
            if (e instanceof feign.RetryableException || e.status() >= 500) {
                throw new RetryableApiCallException(e);
            }
            throw new NonRetryableApiCallException(e);
        }
    }

    @Override
    @CircuitBreaker(name = "pg-api-find", fallbackMethod = "findPaymentsByOrderIdFallback")
    @Retry(name = "pg-api-find")
    public Optional<FindPaymentsResponse> findTransactionsByOrderId(Long orderId) {
        try {
            PgApiDto.OrderResponse response = pgClient.findTransactionsByOrderId(pgUserId, orderId);
            if (response.isFail() || response.data() == null) {
                return Optional.empty();
            }

            List<TransactionInfo> domainTransactions = response.data().transactions().stream()
                    .map(tx -> new TransactionInfo(tx.transactionKey(), tx.status().toPaymentStatus()))
                    .toList();

            return Optional.of(new FindPaymentsResponse(orderId, domainTransactions));
        } catch (FeignException e) {
            log.error("주문 ID로 결제 내역 조회 중 FeignException 발생. orderId: {}, status: {}", orderId, e.status());
            if (e instanceof feign.RetryableException || e.status() >= 500) {
                throw new RetryableApiCallException(e);
            }
            throw new NonRetryableApiCallException(e);
        }
    }

    private Optional<TransactionInfo> requestPaymentFallback(PaymentRequest request, Throwable t) {
        log.warn("requestPayment 실패로 인한 써킷브레이커 실행. orderId: {}, rootCause: {}",
                request.orderId(), t.getClass().getSimpleName());
        return Optional.empty();
    }

    private Optional<FindPaymentsResponse> findPaymentsByOrderIdFallback(Long orderId, Throwable t) {
        log.warn("findPaymentsByOrderId 실패로 인한 써킷브레이커 실행. orderId: {}, rootCause: {}",
                orderId, t.getClass().getSimpleName());
        return Optional.empty();
    }
}

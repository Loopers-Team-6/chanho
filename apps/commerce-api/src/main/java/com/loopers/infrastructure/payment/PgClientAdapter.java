package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.CardPaymentClient;
import com.loopers.infrastructure.payment.dto.PgApiDto;
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
    public Optional<PaymentResponse> requestPayment(PaymentRequest request) {
        PgApiDto.Request pgRequest = new PgApiDto.Request(
                request.orderId(),
                request.cardType(),
                request.cardNo(),
                request.amount().toPlainString(),
                request.callbackUrl()
        );

        try {
            PgApiDto.TransactionResponse response = pgClient.createPaymentRequest(pgUserId, pgRequest);

            if (response.isFail()) {
                return Optional.empty();
            }

            return Optional.of(new PaymentResponse(
                    response.data().transactionKey(),
                    response.data().status().toPaymentStatus()
            ));
        } catch (Exception e) {
            log.error("PG 결제 요청 실패: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<FindPaymentsResponse> findPaymentsByOrderId(Long orderId) {
        try {
            PgApiDto.OrderResponse response = pgClient.findPaymentsByOrderId(pgUserId, orderId);
            if (response.isFail() || response.data() == null) {
                return Optional.empty();
            }

            List<TransactionInfo> domainTransactions = response.data().transactions().stream()
                    .map(tx -> new TransactionInfo(tx.transactionKey(), tx.status().toPaymentStatus()))
                    .toList();

            return Optional.of(new FindPaymentsResponse(response.data().orderId(), domainTransactions));

        } catch (Exception e) {
            log.error("주문 ID로 결제 내역 조회 중 오류 발생. orderId: {}", orderId, e);
            return Optional.empty();
        }
    }
}

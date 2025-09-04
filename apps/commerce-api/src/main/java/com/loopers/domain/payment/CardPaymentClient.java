package com.loopers.domain.payment;

import com.loopers.infrastructure.payment.CardType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CardPaymentClient {

    Optional<TransactionInfo> requestPayment(PaymentRequest request);

    Optional<FindPaymentsResponse> findTransactionsByOrderId(Long orderId);

    record PaymentRequest(
            Long orderId,
            BigDecimal amount,
            CardType cardType,
            String cardNo,
            String callbackUrl
    ) {
    }

    record TransactionInfo(
            String transactionKey,
            PaymentStatus status
    ) {
    }

    record FindPaymentsResponse(
            Long orderId,
            List<TransactionInfo> transactions
    ) {
    }
}

package com.loopers.domain.payment;

import com.loopers.interfaces.api.payment.CardType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CardPaymentClient {

    record PaymentRequest(
            Long orderId,
            BigDecimal amount,
            CardType cardType,
            String cardNo,
            String callbackUrl
    ) {
    }

    record PaymentResponse(
            String transactionKey,
            PaymentStatus status
    ) {
    }

    Optional<PaymentResponse> requestPayment(PaymentRequest request);

    record TransactionInfo(
            String transactionKey,
            PaymentStatus status
    ) {
    }

    record FindPaymentsResponse(
            String orderId,
            List<TransactionInfo> transactions
    ) {
    }

    Optional<FindPaymentsResponse> findPaymentsByOrderId(Long orderId);
}

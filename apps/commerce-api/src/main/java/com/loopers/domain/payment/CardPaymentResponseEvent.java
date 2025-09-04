package com.loopers.domain.payment;

import java.math.BigDecimal;

public record CardPaymentResponseEvent(
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        String transactionKey,
        PaymentStatus status
) {
}

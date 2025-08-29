package com.loopers.domain.payment.processor;

import java.math.BigDecimal;

public record CardPaymentRequestEvent(
        Long paymentId,
        Long orderId,
        BigDecimal amount
) {
}

package com.loopers.domain.payment;

public record PaymentProcessedEvent(
        Long orderId,
        Long paymentId,
        PaymentStatus paymentStatus
) {
}

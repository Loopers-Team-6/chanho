package com.loopers.domain.payment;

public record PaymentProcessedEvent(
        Long orderId,
        PaymentStatus paymentStatus
) {
}

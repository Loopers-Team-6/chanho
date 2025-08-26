package com.loopers.domain.order;

import com.loopers.domain.payment.PaymentMethod;

import java.math.BigDecimal;

public record OrderPlacedEvent(
        Long orderId,
        PaymentMethod paymentMethod,
        BigDecimal amount
) {
}

package com.loopers.domain.order.event;

import com.loopers.application.order.OrderItemInfo;
import com.loopers.domain.payment.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

public record OrderPlacedEvent(
        Long orderId,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        List<OrderItemInfo> items
) {
}

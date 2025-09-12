package com.loopers.event.domain.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderCompletedEvent(
        Long orderId,
        Long userId,
        BigDecimal totalAmount,
        List<Item> items
) {

    public record Item(
            Long productId,
            BigDecimal price,
            Integer quantity
    ) {}
}

package com.loopers.domain.order.event;

public record OrderFailedEvent(
        Long orderId
) {
}

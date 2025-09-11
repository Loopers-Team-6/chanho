package com.loopers.event.domain.product;

public record ProductViewedEvent(
        long productId,
        long userId
) {}

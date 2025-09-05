package com.loopers.event.domain.like;

public record LikeChangedEvent(
        Long productId,
        Long userId,
        boolean liked
) {
}

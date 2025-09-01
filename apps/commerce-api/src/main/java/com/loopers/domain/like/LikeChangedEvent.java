package com.loopers.domain.like;

public record LikeChangedEvent(
        Long productId,
        Long userId,
        boolean liked
) {
}

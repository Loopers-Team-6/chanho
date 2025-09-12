package com.loopers.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final StringRedisTemplate redisTemplate;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleViewEvent(ProductMetricEvent event) {
        switch (event.type()) {
            case VIEW -> updateViewScore(event.productId());
            case LIKE -> updateLikeScore(event.productId(), event.count());
            case ORDER -> updateOrderScore(event.productId(), event.count());
        }
    }

    public void updateViewScore(Long productId) {
        updateScore(productId, 1.0); // 조회 점수: 1점
    }

    public void updateLikeScore(Long productId, int count) {
        updateScore(productId, 5.0 * count); // 좋아요 점수: +5점, 취소: -5점
    }

    public void updateOrderScore(Long productId, int quantity) {
        updateScore(productId, 10.0 * quantity); // 주문 점수: 1개당 10점
    }

    private void updateScore(Long productId, double score) {
        String key = "ranking:daily:" + LocalDate.now()
                .format(DATE_FORMATTER);
        redisTemplate.opsForZSet()
                .incrementScore(key, String.valueOf(productId), score);
    }
}

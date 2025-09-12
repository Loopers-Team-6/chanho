package com.loopers.application;

import com.loopers.domain.metric.ProductMetricEntity;
import com.loopers.domain.metric.ProductMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MetricService {

    private final ApplicationEventPublisher eventPublisher;
    private final ProductMetricRepository productMetricRepository;

    @Transactional
    public void updateViewCount(Long productId) {
        ProductMetricEntity metric = findOrCreateMetric(productId);
        metric.increaseViewCount();
        productMetricRepository.save(metric);

        eventPublisher.publishEvent(new ProductMetricEvent(productId, ProductMetricEvent.MetricType.VIEW, 1));
    }

    @Transactional
    public void updateLikeCount(Long productId, boolean isLiked) {
        ProductMetricEntity metric = findOrCreateMetric(productId);
        if (isLiked) {
            metric.increaseLikeCount();
        } else {
            metric.decreaseLikeCount();
        }
        productMetricRepository.save(metric);

        eventPublisher.publishEvent(new ProductMetricEvent(productId, ProductMetricEvent.MetricType.LIKE, isLiked));
    }

    @Transactional
    public void updateOrderMetrics(Long productId, int quantity, BigDecimal price) {
        ProductMetricEntity metric = findOrCreateMetric(productId);
        metric.addOrder(quantity, price);
        productMetricRepository.save(metric);

        eventPublisher.publishEvent(new ProductMetricEvent(productId, ProductMetricEvent.MetricType.ORDER, quantity));
    }

    private ProductMetricEntity findOrCreateMetric(Long productId) {
        LocalDate today = LocalDate.now();
        return productMetricRepository.findByProductIdAndMetricDateWithLock(productId, today)
                .orElse(ProductMetricEntity.create(productId, today));
    }
}

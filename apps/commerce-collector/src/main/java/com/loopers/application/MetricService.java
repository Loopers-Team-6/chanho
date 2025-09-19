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
    public void updateViewCount(Long productId, LocalDate publishedDate) {
        ProductMetricEntity metric = findOrCreateMetric(productId, publishedDate);
        metric.increaseViewCount();
        productMetricRepository.save(metric);

        eventPublisher.publishEvent(new ProductMetricEvent(productId, ProductMetricEvent.MetricType.VIEW, 1));
    }

    @Transactional
    public void updateLikeCount(Long productId, boolean isLiked, LocalDate publishedDate) {
        ProductMetricEntity metric = findOrCreateMetric(productId, publishedDate);
        if (isLiked) {
            metric.increaseLikeCount();
        } else {
            metric.decreaseLikeCount();
        }
        productMetricRepository.save(metric);

        eventPublisher.publishEvent(new ProductMetricEvent(productId, ProductMetricEvent.MetricType.LIKE, isLiked));
    }

    @Transactional
    public void updateOrderMetrics(Long productId, int quantity, BigDecimal price, LocalDate publishedDate) {
        ProductMetricEntity metric = findOrCreateMetric(productId, publishedDate);
        metric.addOrder(quantity, price);
        productMetricRepository.save(metric);

        eventPublisher.publishEvent(new ProductMetricEvent(productId, ProductMetricEvent.MetricType.ORDER, quantity));
    }

    private ProductMetricEntity findOrCreateMetric(Long productId, LocalDate publishedDate) {
        return productMetricRepository.findByProductIdAndMetricDateWithLock(productId, publishedDate)
                .orElse(ProductMetricEntity.create(productId, publishedDate));
    }
}

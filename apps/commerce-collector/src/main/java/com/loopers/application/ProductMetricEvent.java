package com.loopers.application;

public record ProductMetricEvent(
        Long productId,
        MetricType type,
        int count
) {

    public ProductMetricEvent(Long productId, MetricType metricType, boolean isLiked) {
        this(
                productId,
                metricType,
                isLiked
                        ? 1
                        : -1
        );
    }

    public enum MetricType {
        VIEW, LIKE, ORDER
    }
}

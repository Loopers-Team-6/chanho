package com.loopers.domain.metric;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductMetricRepository {

    Optional<ProductMetricEntity> findByProductIdAndMetricDateWithLock(Long productId, LocalDate metricDate);

    ProductMetricEntity save(ProductMetricEntity entity);
}

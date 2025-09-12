package com.loopers.infrastructure.metric;

import com.loopers.domain.metric.ProductMetricEntity;
import com.loopers.domain.metric.ProductMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductMetricRepositoryImpl implements ProductMetricRepository {

    private final ProductMetricJpaRepository productMetricJpaRepository;


    @Override
    public Optional<ProductMetricEntity> findByProductIdAndMetricDateWithLock(Long productId, LocalDate metricDate) {
        return productMetricJpaRepository.findByProductIdAndMetricDate(productId, metricDate);
    }

    @Override
    public ProductMetricEntity save(ProductMetricEntity entity) {
        return productMetricJpaRepository.save(entity);
    }
}

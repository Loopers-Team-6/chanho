package com.loopers.infrastructure.metric;

import com.loopers.domain.metric.ProductMetricEntity;
import com.loopers.domain.metric.ProductMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductMetricRepositoryImpl implements ProductMetricRepository {

    private final ProductMetricJpaRepository productMetricJpaRepository;

    @Override
    public long count() {
        return productMetricJpaRepository.count();
    }

    @Override
    public ProductMetricEntity save(ProductMetricEntity entity) {
        return productMetricJpaRepository.save(entity);
    }

    @Override
    public List<ProductMetricEntity> saveAll(List<ProductMetricEntity> entities) {
        return productMetricJpaRepository.saveAll(entities);
    }
}

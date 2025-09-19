package com.loopers.domain.metric;

import java.util.List;

public interface ProductMetricRepository {

    long count();

    ProductMetricEntity save(ProductMetricEntity entity);

    List<ProductMetricEntity> saveAll(List<ProductMetricEntity> entities);
}

package com.loopers.infrastructure.metric;

import com.loopers.domain.metric.ProductMetricEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductMetricJpaRepository extends JpaRepository<ProductMetricEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProductMetricEntity> findByProductIdAndMetricDate(Long productId, LocalDate metricDate);


}

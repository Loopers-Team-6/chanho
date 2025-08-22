package com.loopers.domain.payment;

import com.loopers.domain.CustomCrudRepository;

import java.util.Optional;

public interface PaymentRepository extends CustomCrudRepository<PaymentEntity> {
    Optional<PaymentEntity> findByOrderId(Long orderId);
}

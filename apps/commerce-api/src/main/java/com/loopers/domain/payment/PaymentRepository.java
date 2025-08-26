package com.loopers.domain.payment;

import com.loopers.domain.CustomCrudRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends CustomCrudRepository<PaymentEntity> {
    Optional<PaymentEntity> findByOrderId(Long orderId);

    Optional<PaymentEntity> findByTransactionKey(String transactionKey);

    List<PaymentEntity> findAllByStatusAndCreatedAtBefore(PaymentStatus paymentStatus, ZonedDateTime threshold);
}

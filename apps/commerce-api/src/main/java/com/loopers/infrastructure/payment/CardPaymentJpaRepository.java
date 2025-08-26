package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.CardPaymentEntity;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface CardPaymentJpaRepository extends JpaRepository<CardPaymentEntity, Long> {
    Optional<PaymentEntity> findByTransactionKey(String transactionKey);

    List<PaymentEntity> findAllByStatusAndCreatedAtBefore(PaymentStatus paymentStatus, ZonedDateTime threshold);
}

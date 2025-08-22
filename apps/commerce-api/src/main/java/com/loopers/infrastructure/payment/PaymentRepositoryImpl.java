package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.infrastructure.AbstractRepositoryImpl;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class PaymentRepositoryImpl extends AbstractRepositoryImpl<PaymentEntity, PaymentJpaRepository> implements PaymentRepository {
    private final CardPaymentJpaRepository cardPaymentRepository;

    public PaymentRepositoryImpl(PaymentJpaRepository jpaRepository, CardPaymentJpaRepository cardPaymentRepository) {
        super(jpaRepository);
        this.cardPaymentRepository = cardPaymentRepository;
    }

    @Override
    public Optional<PaymentEntity> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<PaymentEntity> findByTransactionKey(String transactionKey) {
        return cardPaymentRepository.findByTransactionKey(transactionKey);
    }

    @Override
    public List<PaymentEntity> findAllByStatusAndCreatedAtBefore(PaymentStatus paymentStatus, ZonedDateTime threshold) {
        return cardPaymentRepository.findAllByStatusAndCreatedAtBefore(paymentStatus, threshold);
    }
}

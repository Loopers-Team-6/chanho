package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.infrastructure.AbstractRepositoryImpl;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentRepositoryImpl extends AbstractRepositoryImpl<PaymentEntity, PaymentJpaRepository> implements PaymentRepository {
    public PaymentRepositoryImpl(PaymentJpaRepository jpaRepository) {
        super(jpaRepository);
    }

    @Override
    public Optional<PaymentEntity> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }
}

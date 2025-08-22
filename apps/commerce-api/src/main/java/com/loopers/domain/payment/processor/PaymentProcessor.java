package com.loopers.domain.payment.processor;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentProcessor {

    PaymentMethod getPaymentMethod();

    PaymentEntity createPayment(Long orderId, BigDecimal amount);

    void processPayment(PaymentEntity payment);
}

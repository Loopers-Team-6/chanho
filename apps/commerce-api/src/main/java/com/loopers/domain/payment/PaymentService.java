package com.loopers.domain.payment;

import com.loopers.domain.payment.processor.PaymentProcessor;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final ApplicationEventPublisher eventPublisher;
    private final Map<PaymentMethod, PaymentProcessor> paymentProcessors;
    private final PaymentRepository paymentRepository;

    public PaymentService(
            ApplicationEventPublisher eventPublisher,
            List<PaymentProcessor> paymentProcessors,
            PaymentRepository paymentRepository) {
        this.eventPublisher = eventPublisher;
        this.paymentProcessors = paymentProcessors.stream()
                .collect(Collectors.toUnmodifiableMap(PaymentProcessor::getPaymentMethod, Function.identity()));
        this.paymentRepository = paymentRepository;
    }

    public void processPayment(Long orderId, PaymentMethod paymentMethod, BigDecimal amount) {
        PaymentProcessor paymentProcessor = paymentProcessors.get(paymentMethod);
        if (paymentProcessor == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }

        PaymentEntity payment = paymentRepository.save(paymentProcessor.createPayment(orderId, amount));
        paymentProcessor.processPayment(payment);
        paymentRepository.save(payment);

        eventPublisher.publishEvent(new PaymentProcessedEvent(payment.getId(), payment.getStatus()));
    }

    public PaymentEntity findByOrderId(Long id) {
        return paymentRepository.findByOrderId(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for order ID: " + id));
    }
}

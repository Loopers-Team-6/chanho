package com.loopers.domain.payment;

import com.loopers.application.payment.PaymentCommand;
import com.loopers.domain.payment.processor.PaymentProcessor;
import com.loopers.interfaces.api.payment.TransactionStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public void createPayment(Long orderId, PaymentMethod paymentMethod, BigDecimal amount) {
        PaymentProcessor paymentProcessor = getPaymentProcessor(paymentMethod);

        PaymentEntity payment = paymentRepository.save(paymentProcessor.createPayment(orderId, amount));
        paymentProcessor.processPayment(payment);
        paymentRepository.save(payment);

        eventPublisher.publishEvent(new PaymentProcessedEvent(orderId, payment.getId(), payment.getStatus()));
    }

    @Transactional
    public void processPaymentCallback(PaymentCommand.Update command) {
        if (command == null) {
            throw new IllegalArgumentException("Invalid payment command");
        }
        if (command.status() == TransactionStatus.PENDING) {
            return;
        }

        PaymentEntity payment = findByTransactionKey(command.transactionKey());
        switch (command.status()) {
            case SUCCESS -> payment.complete();
            case FAILED -> payment.fail();
        }

        paymentRepository.save(payment);
        eventPublisher.publishEvent(new PaymentProcessedEvent(payment.getOrderId(), payment.getId(), payment.getStatus()));
    }

    public PaymentEntity findByOrderId(Long id) {
        return paymentRepository.findByOrderId(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for order ID: " + id));
    }

    public PaymentEntity findByTransactionKey(String transactionKey) {
        return paymentRepository.findByTransactionKey(transactionKey)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for transaction key: " + transactionKey));
    }

    private PaymentProcessor getPaymentProcessor(PaymentMethod paymentMethod) {
        PaymentProcessor processor = paymentProcessors.get(paymentMethod);
        if (processor == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
        return processor;
    }
}

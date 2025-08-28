package com.loopers.domain.payment;

import com.loopers.application.payment.PaymentCommand;
import com.loopers.domain.payment.processor.PaymentProcessor;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
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

    @Transactional
    public void requestPayment(Long orderId, PaymentMethod paymentMethod, BigDecimal amount) {
        PaymentProcessor paymentProcessor = getPaymentProcessor(paymentMethod);

        try {
            PaymentEntity payment = paymentRepository.save(paymentProcessor.createPayment(orderId, amount));
            paymentProcessor.processPayment(payment);
            paymentRepository.save(payment);

            eventPublisher.publishEvent(new PaymentProcessedEvent(orderId, payment.getId(), payment.getStatus()));
        } catch (DataIntegrityViolationException e) {
            log.warn("결제 요청 중복 발생 Order ID: [{}], Payment Method: [{}]", orderId, paymentMethod);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAndSyncStatus(PaymentEntity payment) {
        PaymentProcessor paymentProcessor = getPaymentProcessor(payment.getMethod());
        paymentProcessor.processPayment(payment);
        paymentRepository.save(payment);

        eventPublisher.publishEvent(new PaymentProcessedEvent(payment.getOrderId(), payment.getId(), payment.getStatus()));
    }

    @Transactional
    public void confirmPayment(PaymentCommand.Update command) {
        if (command == null) {
            throw new IllegalArgumentException("Invalid payment command");
        }
        if (command.status() == PaymentStatus.PENDING) {
            return;
        }

        PaymentEntity payment = findByTransactionKey(command.transactionKey());
        switch (command.status()) {
            case SUCCESS -> payment.markAsSuccess();
            case FAILED -> payment.markAsFailed();
        }

        paymentRepository.save(payment);
        eventPublisher.publishEvent(new PaymentProcessedEvent(payment.getOrderId(), payment.getId(), payment.getStatus()));
    }

    public List<PaymentEntity> findPaymentsToRetry(ZonedDateTime threshold) {
        return paymentRepository.findAllByStatusAndCreatedAtBefore(PaymentStatus.CREATED, threshold);
    }

    public List<PaymentEntity> findPendingPayments(ZonedDateTime threshold) {
        return paymentRepository.findAllByStatusAndCreatedAtBefore(PaymentStatus.PENDING, threshold);
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

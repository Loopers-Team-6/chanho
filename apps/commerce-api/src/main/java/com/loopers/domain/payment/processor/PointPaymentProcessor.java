package com.loopers.domain.payment.processor;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentProcessedEvent;
import com.loopers.domain.payment.PointPaymentEntity;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class PointPaymentProcessor implements PaymentProcessor {

    private final ApplicationEventPublisher eventPublisher;
    private final PointService pointService;
    private final OrderService orderService;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.POINT;
    }

    @Override
    public PaymentEntity createPayment(Long orderId, BigDecimal amount) {
        return PointPaymentEntity.create(orderId, amount);
    }

    @Override
    public void processPayment(PaymentEntity payment) {
        OrderEntity order = orderService.findById(payment.getOrderId());

        Long userId = order.getUser().getId();
        BigDecimal finalPrice = payment.getAmount();

        try {
            pointService.deductPoints(userId, finalPrice);
            payment.markAsSuccess();
        } catch (RuntimeException e) {
            log.error("포인트 결제 실패: ", e);
            payment.markAsFailed();
            orderService.save(order);
        }

        eventPublisher.publishEvent(new PaymentProcessedEvent(payment.getOrderId(), payment.getStatus()));
    }
}

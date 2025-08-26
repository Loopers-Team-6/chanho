package com.loopers.domain.payment.processor;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PointPaymentEntity;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PointPaymentProcessor implements PaymentProcessor {

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
        BigDecimal finalPrice = order.getFinalPrice();

        try {
            pointService.deductPoints(userId, finalPrice);
            payment.markAsSuccess();
        } catch (IllegalStateException e) {
            payment.markAsFailed();
            order.fail();
            orderService.save(order);
        }
    }
}

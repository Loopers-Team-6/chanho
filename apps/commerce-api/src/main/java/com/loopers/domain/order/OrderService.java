package com.loopers.domain.order;

import com.loopers.domain.payment.PaymentStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderEntity save(OrderEntity order) {
        return orderRepository.save(order);
    }

    public OrderEntity findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    }

    public void updateOrderStatus(Long orderId, PaymentStatus status) {
        if (status == PaymentStatus.PENDING) {
            return;
        }

        OrderEntity order = findById(orderId);
        switch (status) {
            case SUCCESS -> order.complete();
            case FAILED -> order.fail();
            case CANCELED -> order.cancel();
        }
        save(order);
    }
}

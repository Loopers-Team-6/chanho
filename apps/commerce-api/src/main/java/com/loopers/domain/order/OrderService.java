package com.loopers.domain.order;

import com.loopers.domain.order.event.OrderFailedEvent;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.product.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;

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

        OrderEntity order = orderRepository.findByIdWithPessimisticLock(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        switch (status) {
            case SUCCESS -> order.complete();
            case FAILED -> {
                order.fail();
                if (order.isStockDeducted()) {
                    eventPublisher.publishEvent(new OrderFailedEvent(orderId));
                }
            }
            case CANCELED -> {
                order.cancel();
                if (order.isStockDeducted()) {
                    eventPublisher.publishEvent(new OrderFailedEvent(orderId));
                }
            }
        }
        save(order);
    }
}

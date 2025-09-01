package com.loopers.domain.order;

import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

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
            case FAILED -> {
                order.fail();
                restoreStock(orderId);
            }
            case CANCELED -> {
                order.cancel();
                restoreStock(orderId);
            }
        }
        save(order);
    }

    private void restoreStock(Long orderId) {
        log.info("재고 복원 시작: orderId={}", orderId);

        OrderEntity order = orderRepository.findByIdWithPessimisticLock(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        if (!order.isStockDeducted()) {
            log.info("재고가 차감된 기록이 없어 복원을 건너뜁니다: orderId={}", orderId);
            return;
        }

        order.getItems().forEach(item -> {
            ProductEntity product = productService.findById(item.getProductId());
            product.increaseStock(item.getQuantity());
        });

        order.markStockAsRestored();
        orderRepository.save(order);
        log.info("재고 복원 완료: orderId={}", orderId);
    }
}

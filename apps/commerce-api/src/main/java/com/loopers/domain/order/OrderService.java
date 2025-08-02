package com.loopers.domain.order;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.user.UserEntity;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderEntity createOrder(UserEntity user) {
        OrderEntity order = OrderEntity.create(user);
        return orderRepository.save(order);
    }

    public BigDecimal calculateTotalPrice(Map<ProductEntity, Integer> productQuantities) {
        return productQuantities.entrySet().stream()
                .map(entry -> entry.getKey().getPrice().multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public OrderEntity save(OrderEntity order) {
        return orderRepository.save(order);
    }

}

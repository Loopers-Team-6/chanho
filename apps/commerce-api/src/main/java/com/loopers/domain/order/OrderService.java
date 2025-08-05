package com.loopers.domain.order;

import com.loopers.domain.product.ProductEntity;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderEntity save(OrderEntity order) {
        return orderRepository.save(order);
    }

    public void addOrderItems(OrderEntity order, Map<ProductEntity, Integer> productQuantities) {
        productQuantities.forEach((product, quantity) ->
                order.addOrderItem(product.getId(), product.getName(), product.getPrice(), quantity));
    }
}

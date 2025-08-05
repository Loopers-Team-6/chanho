package com.loopers.domain.order;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderEntity save(OrderEntity order) {
        return orderRepository.save(order);
    }

}

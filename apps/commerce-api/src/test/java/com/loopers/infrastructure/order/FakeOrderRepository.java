package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderRepository;
import com.loopers.infrastructure.InMemoryCrudRepository;

public class FakeOrderRepository extends InMemoryCrudRepository<OrderEntity> implements OrderRepository {
}

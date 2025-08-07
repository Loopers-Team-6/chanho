package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.infrastructure.InMemoryCrudRepository;

public class FakeOrderRepository extends InMemoryCrudRepository<OrderEntity> implements OrderRepository {
    @Override
    public long countByUser(UserEntity user) {
        return findAll().stream()
                .filter(order -> order.getUser().equals(user))
                .count();
    }
}

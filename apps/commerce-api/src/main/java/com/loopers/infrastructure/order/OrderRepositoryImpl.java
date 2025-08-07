package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.infrastructure.AbstractRepositoryImpl;
import org.springframework.stereotype.Component;

@Component
public class OrderRepositoryImpl extends AbstractRepositoryImpl<OrderEntity, OrderJpaRepository> implements OrderRepository {
    public OrderRepositoryImpl(OrderJpaRepository jpaRepository) {
        super(jpaRepository);
    }

    @Override
    public long countByUser(UserEntity user) {
        return jpaRepository.countByUser(user);
    }
}

package com.loopers.domain.order;

import com.loopers.domain.CustomCrudRepository;
import com.loopers.domain.user.UserEntity;

public interface OrderRepository extends CustomCrudRepository<OrderEntity> {
    long countByUser(UserEntity user);
}

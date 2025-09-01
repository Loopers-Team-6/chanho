package com.loopers.domain.order;

import com.loopers.domain.CustomCrudRepository;
import com.loopers.domain.user.UserEntity;

import java.util.Optional;

public interface OrderRepository extends CustomCrudRepository<OrderEntity> {
    long countByUser(UserEntity user);

    Optional<OrderEntity> findByIdWithPessimisticLock(Long orderId);
}

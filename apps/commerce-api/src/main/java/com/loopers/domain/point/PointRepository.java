package com.loopers.domain.point;

import com.loopers.domain.CustomCrudRepository;

import java.util.Optional;

public interface PointRepository extends CustomCrudRepository<PointEntity> {
    Optional<PointEntity> findByUserId(Long id);

    Optional<PointEntity> findByUserIdWithPessimisticLock(long userId);
}

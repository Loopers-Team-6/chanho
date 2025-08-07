package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.infrastructure.InMemoryCrudRepository;

import java.util.Optional;

public class FakePointRepository extends InMemoryCrudRepository<PointEntity> implements PointRepository {
    @Override
    public Optional<PointEntity> findByUserId(Long id) {
        return map.values().stream()
                .filter(point -> point.getUser().getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<PointEntity> findByUserIdWithPessimisticLock(long userId) {
        return map.values().stream()
                .filter(point -> point.getUser().getId().equals(userId))
                .findFirst();
    }
}

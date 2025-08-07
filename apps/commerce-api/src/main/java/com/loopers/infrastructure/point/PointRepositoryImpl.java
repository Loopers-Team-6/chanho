package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.infrastructure.AbstractRepositoryImpl;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PointRepositoryImpl extends AbstractRepositoryImpl<PointEntity, PointJpaRepository> implements PointRepository {

    public PointRepositoryImpl(PointJpaRepository jpaRepository) {
        super(jpaRepository);
    }

    @Override
    public Optional<PointEntity> findByUserId(Long id) {
        return jpaRepository.findByUserId(id);
    }

    @Override
    public Optional<PointEntity> findByUserIdWithPessimisticLock(long userId) {
        return jpaRepository.findByUserIdWithPessimisticLock(userId);
    }
}

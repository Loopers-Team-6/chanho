package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {
    private final PointJpaRepository pointJpaRepository;

    @Override
    public PointEntity save(PointEntity point) {
        return pointJpaRepository.save(point);
    }

    @Override
    public Optional<PointEntity> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<PointEntity> findByIdIn(List<Long> ids) {
        return List.of();
    }

    @Override
    public List<PointEntity> findAll() {
        return List.of();
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public Optional<PointEntity> findByUserId(Long id) {
        return pointJpaRepository.findByUserId(id);
    }
}

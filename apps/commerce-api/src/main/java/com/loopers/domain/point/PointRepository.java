package com.loopers.domain.point;

import com.loopers.domain.CrudRepository;

import java.util.Optional;

public interface PointRepository extends CrudRepository<PointEntity> {
    Optional<PointEntity> findByUserId(Long id);
}

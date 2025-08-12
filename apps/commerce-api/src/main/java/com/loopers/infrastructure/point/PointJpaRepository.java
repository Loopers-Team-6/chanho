package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<PointEntity, Long> {

    Optional<PointEntity> findByUserId(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PointEntity p where p.user.id = :userId")
    Optional<PointEntity> findByUserIdWithPessimisticLock(@Param("userId") Long userId);
}

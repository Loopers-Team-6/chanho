package com.loopers.infrastructure.log;

import com.loopers.domain.log.EventLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogJpaRepository extends JpaRepository<EventLogEntity, Long> {

    boolean existsByEventId(String eventId);
}

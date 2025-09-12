package com.loopers.infrastructure.log;

import com.loopers.domain.log.EventLogEntity;
import com.loopers.domain.log.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventLogRepositoryImpl implements EventLogRepository {

    private final EventLogJpaRepository eventLogJpaRepository;

    @Override
    public boolean existsByEventId(String eventId) {
        return eventLogJpaRepository.existsByEventId(eventId);
    }

    @Override
    public EventLogEntity save(EventLogEntity entity) {
        return eventLogJpaRepository.save(entity);
    }
}

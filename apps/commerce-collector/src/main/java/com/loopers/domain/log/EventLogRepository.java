package com.loopers.domain.log;

public interface EventLogRepository {

    boolean existsByEventId(String eventId);

    EventLogEntity save(EventLogEntity entity);
}

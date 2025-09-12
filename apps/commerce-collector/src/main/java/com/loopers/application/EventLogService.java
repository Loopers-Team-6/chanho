package com.loopers.application;

import com.loopers.domain.log.EventLogEntity;
import com.loopers.domain.log.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventLogService {

    private final EventLogRepository eventLogRepository;

    @Transactional
    public void recordEvent(String eventId, String topic, String payload) {
        if (isEventProcessed(eventId)) {
            return;
        }

        eventLogRepository.save(EventLogEntity.create(eventId, topic, payload));
    }

    public boolean isEventProcessed(String eventId) {
        return eventLogRepository.existsByEventId(eventId);
    }
}

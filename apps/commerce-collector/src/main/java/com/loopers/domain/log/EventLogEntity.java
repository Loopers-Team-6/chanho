package com.loopers.domain.log;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "event_logs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_event_logs_event_id",
                        columnNames = {"eventId"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventLogEntity extends BaseEntity {

    @Column(
            name = "event_id",
            nullable = false,
            updatable = false
    )
    private String eventId;

    @Column(
            name = "topic",
            nullable = false,
            updatable = false
    )
    private String topic;

    @Lob
    @Column(
            name = "payload",
            nullable = false,
            updatable = false
    )
    private String payload;

    private EventLogEntity(String eventId, String topic, String payload) {
        this.eventId = eventId;
        this.topic = topic;
        this.payload = payload;
    }

    public static EventLogEntity create(String eventId, String topic, String payload) {
        return new EventLogEntity(eventId, topic, payload);
    }
}

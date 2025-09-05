package com.loopers.config;

import java.time.LocalDateTime;
import java.util.UUID;

public record KafkaMessage<T>(
        String eventId,
        String version,
        LocalDateTime publishedAt,
        T payload
) {
    public static <T> KafkaMessage<T> of(T payload) {
        return new KafkaMessage<>(UUID.randomUUID().toString(), "v1", LocalDateTime.now(), payload);
    }
}

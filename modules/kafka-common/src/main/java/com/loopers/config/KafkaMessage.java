package com.loopers.config;

import java.time.ZonedDateTime;
import java.util.UUID;

public record KafkaMessage<T>(
        String eventId,
        String version,
        ZonedDateTime publishedAt,
        T payload
) {

    public static <T> KafkaMessage<T> of(T payload) {
        return new KafkaMessage<>(UUID.randomUUID().toString(), "v1", ZonedDateTime.now(), payload);
    }
}

package com.loopers.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // 카프카 브로커 주소
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // 직렬화
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // acks=all: 리더 파티션과 모든 In-Sync Replica가 메시지를 받아야만 성공으로 인정
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");

        // 멱등성 활성화: 중복 메시지 전송 방지
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // 재시도
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE); // 횟수 무제한
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100); // 간격 100ms
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000); // 최대 120초 대기 - 지나면 재시도하지 않음

        // 배치
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16KB 배치 크기
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5); // 최대 5ms 대기 후 전송

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // 토픽명 규칙
    // {서비스명}.{도메인}.{이벤트타입}.v{버전}
    public static class Topic {
        public static final String LIKE_CHANGED = "commerce.like.changed.v1";
    }
}

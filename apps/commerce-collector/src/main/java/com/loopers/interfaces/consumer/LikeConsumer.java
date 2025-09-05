package com.loopers.interfaces.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.config.KafkaMessage;
import com.loopers.config.KafkaTopic;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.event.domain.like.LikeChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopic.Like.LIKE_CHANGED,
            groupId = "test-consumer-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleLikeChangedEvents(
            List<ConsumerRecord<String, String>> records,
            Acknowledgment acknowledgment
    ) {
        log.info("수신된 메시지 수: {}", records.size());

        try {
            for (ConsumerRecord<String, String> record : records) {
                TypeReference<KafkaMessage<LikeChangedEvent>> typeRef = new TypeReference<>() {};
                KafkaMessage<LikeChangedEvent> message = objectMapper.readValue(record.value(), typeRef);

                log.info(message.toString());
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생", e);
            acknowledgment.acknowledge();
        }
    }
}

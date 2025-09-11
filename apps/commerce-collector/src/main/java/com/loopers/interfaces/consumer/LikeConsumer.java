package com.loopers.interfaces.consumer;

import com.loopers.config.KafkaMessage;
import com.loopers.config.KafkaTopic;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.event.domain.like.LikeChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeConsumer {

    @KafkaListener(
            topics = KafkaTopic.Catalog.LIKE_CHANGED,
            groupId = "test-consumer-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleLikeChangedEvents(List<KafkaMessage<LikeChangedEvent>> messages, Acknowledgment acknowledgment) {
        log.info("수신된 메시지 수: {}", messages.size());

        try {
            for (KafkaMessage<LikeChangedEvent> message : messages) {
                log.info(message.toString());
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생", e);
            acknowledgment.acknowledge();
        }
    }
}

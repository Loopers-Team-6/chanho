package com.loopers.interfaces.event;

import com.loopers.config.KafkaConfig;
import com.loopers.domain.like.LikeChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LikeChangedEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeChangedEventToKafka(LikeChangedEvent event) {
        kafkaTemplate.send(KafkaConfig.Topic.LIKE_CHANGED, "product:" + event.productId(), event);
    }
}

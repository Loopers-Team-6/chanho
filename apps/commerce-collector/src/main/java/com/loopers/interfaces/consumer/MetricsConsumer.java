package com.loopers.interfaces.consumer;

import com.loopers.application.EventLogService;
import com.loopers.application.MetricService;
import com.loopers.config.KafkaMessage;
import com.loopers.config.KafkaTopic;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.event.domain.like.LikeChangedEvent;
import com.loopers.event.domain.order.OrderCompletedEvent;
import com.loopers.event.domain.product.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsConsumer {

    private static final String CONSUMER_GROUP = "metrics-consumer-group";
    private final EventLogService eventLogService;
    private final MetricService metricService;

    @KafkaListener(
            topics = KafkaTopic.Catalog.LIKE_CHANGED,
            groupId = CONSUMER_GROUP,
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleLikeChangedEvents(List<KafkaMessage<LikeChangedEvent>> messages, Acknowledgment acknowledgment) {
        log.info("수신된 메시지 수: {}", messages.size());

        try {
            for (KafkaMessage<LikeChangedEvent> message : messages) {
                if (isEventProcessed(message)) {
                    continue;
                }

                recordEvent(message, KafkaTopic.Catalog.LIKE_CHANGED);
                LikeChangedEvent event = message.payload();
                metricService.updateLikeCount(
                        event.productId(),
                        event.isLiked(),
                        message.publishedAt()
                                .toLocalDate()
                );
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생", e);
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(
            topics = KafkaTopic.Catalog.PRODUCT_VIEWED,
            groupId = CONSUMER_GROUP,
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleProductViewedEvents(List<KafkaMessage<ProductViewedEvent>> messages, Acknowledgment acknowledgment) {
        log.info("수신된 메시지 수: {}", messages.size());

        try {
            for (KafkaMessage<ProductViewedEvent> message : messages) {
                if (isEventProcessed(message)) {
                    continue;
                }

                recordEvent(message, KafkaTopic.Catalog.PRODUCT_VIEWED);
                ProductViewedEvent event = message.payload();
                metricService.updateViewCount(
                        event.productId(),
                        message.publishedAt()
                                .toLocalDate()
                );
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생", e);
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(
            topics = KafkaTopic.Catalog.ORDER_COMPLETED,
            groupId = CONSUMER_GROUP,
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleOrderCompletedEvents(List<KafkaMessage<OrderCompletedEvent>> messages, Acknowledgment acknowledgment) {
        log.info("수신된 메시지 수: {}", messages.size());

        try {
            for (KafkaMessage<OrderCompletedEvent> message : messages) {
                if (isEventProcessed(message)) {
                    continue;
                }

                recordEvent(message, KafkaTopic.Catalog.PRODUCT_VIEWED);
                OrderCompletedEvent event = message.payload();
                for (OrderCompletedEvent.Item item : event.items()) {
                    metricService.updateOrderMetrics(
                            item.productId(),
                            item.quantity(),
                            item.price(),
                            message.publishedAt()
                                    .toLocalDate()
                    );
                }
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생", e);
            acknowledgment.acknowledge();
        }
    }

    private <T> boolean isEventProcessed(KafkaMessage<T> message) {
        String eventId = message.eventId();
        if (eventLogService.isEventProcessed(eventId)) {
            log.info("이미 처리된 이벤트입니다. eventId: {}", eventId);
            return true;
        }
        return false;
    }

    private void recordEvent(KafkaMessage<?> message, String topic) {
        eventLogService.recordEvent(message.eventId(), topic, message.toString());
    }
}

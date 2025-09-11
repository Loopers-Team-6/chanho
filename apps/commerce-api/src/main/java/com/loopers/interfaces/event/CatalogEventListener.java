package com.loopers.interfaces.event;

import com.loopers.config.KafkaMessage;
import com.loopers.config.KafkaTopic;
import com.loopers.domain.order.event.OrderCompletedEvent;
import com.loopers.event.domain.like.LikeChangedEvent;
import com.loopers.event.domain.product.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CatalogEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductViewedEvent event) {
        KafkaMessage<ProductViewedEvent> message = KafkaMessage.of(event);
        kafkaTemplate.send(KafkaTopic.Catalog.PRODUCT_VIEWED, "product:" + event.productId(), message);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(LikeChangedEvent event) {
        KafkaMessage<LikeChangedEvent> msg = KafkaMessage.of(event);
        kafkaTemplate.send(KafkaTopic.Catalog.LIKE_CHANGED, "product:" + event.productId(), msg);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderCompletedEvent event) {
        KafkaMessage<OrderCompletedEvent> msg = KafkaMessage.of(event);
        kafkaTemplate.send(KafkaTopic.Catalog.ORDER_COMPLETED, "order:" + event.orderId(), msg);
    }
}

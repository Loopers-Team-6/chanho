package com.loopers.interfaces.event;

import com.loopers.domain.order.event.OrderFailedEvent;
import com.loopers.domain.order.event.OrderPlacedEvent;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StockEventListener {

    private final ProductService productService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        List<ProductCommand.StockDecrease> decreaseCommands = event.items().stream()
                .map(item -> new ProductCommand.StockDecrease(item.productId(), item.quantity()))
                .toList();

        productService.decreaseStocks(event.orderId(), decreaseCommands);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderFailedEvent(OrderFailedEvent event) {
        productService.restoreStocks(event.orderId());
    }
}

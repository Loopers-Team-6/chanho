package com.loopers.domain.product;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ProductEntity findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("상품 ID는 null일 수 없습니다");
        }
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상품을 찾을 수 없습니다: " + id));
    }

    public Page<ProductEntity> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public List<ProductEntity> findAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("상품 ID 목록은 비어있을 수 없습니다");
        }
        return productRepository.findAllById(ids);
    }

    @Transactional
    public void updateLikeCount(Long productId, boolean liked) {
        ProductEntity product = productRepository.findByIdWithPessimisticLock(productId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상품을 찾을 수 없습니다: " + productId));
        if (liked) {
            product.increaseLikeCount();
        } else {
            product.decreaseLikeCount();
        }
        productRepository.save(product);
    }

    @Transactional
    public void decreaseStocks(Long orderId, List<ProductCommand.StockDecrease> commands) {
        if (commands == null || commands.isEmpty()) {
            return;
        }

        OrderEntity order = orderRepository.findByIdWithPessimisticLock(orderId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 주문을 찾을 수 없습니다: " + orderId));

        if (order.isStockDeducted() || !order.isPending()) {
            log.warn("이미 재고가 차감되었거나 주문이 PENDING 상태가 아니므로 재고 차감을 건너뜁니다. status={}, stockDeducted={}",
                    order.getStatus(), order.isStockDeducted());
            return;
        }

        for (ProductCommand.StockDecrease command : commands) {
            ProductEntity product = productRepository.findByIdWithPessimisticLock(command.productId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상품을 찾을 수 없습니다: " + command.productId()));

            product.decreaseStock(command.quantity());
        }

        order.markStockAsDeducted();
        orderRepository.save(order);
    }

    public void restoreStocks(Long orderId) {
        log.info("재고 복원 시작: orderId={}", orderId);

        OrderEntity order = orderRepository.findByIdWithPessimisticLock(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        if (!order.isStockDeducted()) {
            log.info("재고가 차감된 기록이 없어 복원을 건너뜁니다: orderId={}", orderId);
            return;
        }

        order.getItems().forEach(item -> {
            ProductEntity product = productRepository.findByIdWithPessimisticLock(item.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상품을 찾을 수 없습니다: " + item.getProductId()));
            product.increaseStock(item.getQuantity());
        });

        order.markStockAsRestored();
        orderRepository.save(order);
        log.info("재고 복원 완료: orderId={}", orderId);
    }

    public List<ProductEntity> findAllByIdWithPessimisticLock(List<Long> productIds) {
        return productRepository.findAllByIdWithPessimisticLock(productIds);
    }
}

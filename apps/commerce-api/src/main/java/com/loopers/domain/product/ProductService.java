package com.loopers.domain.product;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

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
    public void decreaseStocks(List<ProductCommand.StockDecrease> commands) {
        if (commands == null || commands.isEmpty()) {
            return;
        }

        for (ProductCommand.StockDecrease command : commands) {
            ProductEntity product = productRepository.findByIdWithPessimisticLock(command.productId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상품을 찾을 수 없습니다: " + command.productId()));

            product.decreaseStock(command.quantity());
        }
    }
}

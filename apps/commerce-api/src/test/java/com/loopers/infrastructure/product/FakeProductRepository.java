package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class FakeProductRepository implements ProductRepository {

    private final Map<Long, ProductEntity> productRepository = new ConcurrentHashMap<>();

    @Override
    public ProductEntity save(ProductEntity productEntity) {
        ProductEntity product = productRepository.putIfAbsent(productEntity.getId(), productEntity);
        return product == null ? productEntity : product;
    }

    @Override
    public Optional<ProductEntity> findById(Long productId) {
        return Optional.ofNullable(productRepository.get(productId));
    }
}

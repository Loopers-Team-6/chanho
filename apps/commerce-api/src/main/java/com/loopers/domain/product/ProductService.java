package com.loopers.domain.product;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductEntity> findAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("상품 ID 목록은 비어있을 수 없습니다");
        }
        return productRepository.findAllById(ids);
    }
}

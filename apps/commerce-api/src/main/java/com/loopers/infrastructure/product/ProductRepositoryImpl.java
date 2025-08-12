package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.infrastructure.AbstractRepositoryImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ProductRepositoryImpl extends AbstractRepositoryImpl<ProductEntity, ProductJpaRepository> implements ProductRepository {
    public ProductRepositoryImpl(ProductJpaRepository jpaRepository) {
        super(jpaRepository);
    }

    @Override
    public Page<ProductEntity> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }
}

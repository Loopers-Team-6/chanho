package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.infrastructure.AbstractRepositoryImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductRepositoryImpl extends AbstractRepositoryImpl<ProductEntity, ProductJpaRepository> implements ProductRepository {
    public ProductRepositoryImpl(ProductJpaRepository jpaRepository) {
        super(jpaRepository);
    }

    @Override
    public Page<ProductEntity> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public Optional<ProductEntity> findByIdWithPessimisticLock(Long id) {
        return jpaRepository.findByIdWithPessimisticLock(id);
    }

    @Override
    public List<ProductEntity> findAllByIdWithPessimisticLock(List<Long> productIds) {
        return jpaRepository.findAllByIdWithPessimisticLock(productIds);
    }
}

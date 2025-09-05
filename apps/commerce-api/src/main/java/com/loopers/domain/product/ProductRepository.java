package com.loopers.domain.product;

import com.loopers.domain.CustomCrudRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends CustomCrudRepository<ProductEntity> {

    Page<ProductEntity> findAll(Pageable pageable);

    Optional<ProductEntity> findByIdWithPessimisticLock(Long id);

    List<ProductEntity> findAllByIdWithPessimisticLock(List<Long> productIds);
}

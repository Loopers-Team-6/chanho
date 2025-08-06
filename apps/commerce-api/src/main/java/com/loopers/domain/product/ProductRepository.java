package com.loopers.domain.product;

import com.loopers.domain.CustomCrudRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository extends CustomCrudRepository<ProductEntity> {

    Page<ProductEntity> findAll(Pageable pageable);

}

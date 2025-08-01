package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.infrastructure.InMemoryCrudRepository;

public class FakeProductRepository extends InMemoryCrudRepository<ProductEntity> implements ProductRepository {
}

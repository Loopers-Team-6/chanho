package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.infrastructure.InMemoryCrudRepository;

public class FakeBrandRepository extends InMemoryCrudRepository<BrandEntity> implements BrandRepository {
}

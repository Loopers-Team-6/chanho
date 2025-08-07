package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.infrastructure.AbstractRepositoryImpl;
import org.springframework.stereotype.Component;

@Component
public class BrandRepositoryImpl extends AbstractRepositoryImpl<BrandEntity, BrandJpaRepository> implements BrandRepository {
    public BrandRepositoryImpl(BrandJpaRepository jpaRepository) {
        super(jpaRepository);
    }
}

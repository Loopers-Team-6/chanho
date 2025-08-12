package com.loopers.infrastructure.product;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class TestDataSetupService {

    private static final Random RANDOM = new Random(12345L);
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final EntityManager entityManager;

    @Transactional
    public void setupData(int productCount, int brandCount) {
        if (productRepository.count() > 0) {
            System.out.println("데이터가 이미 존재하여 생성을 건너뜁니다.");
            return;
        }

        System.out.println("대용량 테스트 데이터 생성을 시작합니다...");

        // 1. 브랜드 생성
        List<Long> brandIds = new ArrayList<>();
        for (int i = 1; i <= brandCount; i++) {
            BrandEntity saved = brandRepository.save(BrandEntity.create("Brand-" + String.format("%03d", i)));
            brandIds.add(saved.getId());
        }

        // 2. 상품 생성
        int batchSize = 1000;
        int topTierBrandCount = Math.max(1, ((int) (brandCount * 0.1)));
        int topTierProductCount = (int) (productCount * 0.7);

        for (int i = 0; i < productCount; i++) {
            Long brandIdToUse;

            if (i < topTierProductCount) {
                // 상위 70% 상품은 상위 10% 브랜드에 할당
                brandIdToUse = brandIds.get(i % topTierBrandCount);
            } else {
                // 나머지 30% 상품은 나머지 90% 브랜드에 할당
                brandIdToUse = brandIds.get(topTierBrandCount + ((i - topTierProductCount) % (brandCount - topTierBrandCount)));
            }

            BrandEntity reference = entityManager.getReference(BrandEntity.class, brandIdToUse);
            ProductEntity product = createProduct(i + 1, reference);
            entityManager.persist(product);

            // OOM 방지를 위해 배치 사이즈마다 flush 및 clear
            if ((i + 1) % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
                System.out.println(i + 1 + "개의 상품 데이터 생성 완료...");
            }
        }

        entityManager.flush();
        entityManager.clear();
        System.out.println("총 " + productRepository.count() + "개의 상품 데이터 생성이 완료되었습니다.");
    }

    private ProductEntity createProduct(int productSequence, BrandEntity brand) {
        String productName = "Product-" + productSequence;
        int price = (100 + RANDOM.nextInt(900)) * 100;
        int stock = RANDOM.nextInt(100);
        return ProductEntity.create(productName, price, stock, brand);
    }
}

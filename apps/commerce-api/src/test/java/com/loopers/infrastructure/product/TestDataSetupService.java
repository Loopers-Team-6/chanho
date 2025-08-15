package com.loopers.infrastructure.product;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import com.loopers.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class TestDataSetupService {

    private static final Random RANDOM = new Random(12345L);

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Transactional
    public void setupData(int productCount, int brandCount, int userCount) {
        if (productRepository.count() > 0 || userRepository.count() > 0) {
            System.out.println("데이터가 이미 존재하여 생성을 건너뜁니다.");
            return;
        }

        System.out.println("대용량 테스트 데이터 생성을 시작합니다...");

        // 1. 브랜드 생성
        System.out.println("브랜드 데이터 생성 중...");
        List<Long> brandIds = new ArrayList<>();
        for (int i = 1; i <= brandCount; i++) {
            BrandEntity saved = brandRepository.save(BrandEntity.create("Brand-" + String.format("%03d", i)));
            brandIds.add(saved.getId());
        }
        System.out.println(brandCount + "개의 브랜드 데이터 생성 완료.");

        // 2. 상품 생성
        System.out.println("상품 데이터 생성 중...");
        int batchSize = 1000;
        int topTierBrandCount = Math.max(1, ((int) (brandCount * 0.1)));
        int topTierProductCount = (int) (productCount * 0.7);
        List<Long> productIds = new ArrayList<>();

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
            productIds.add(product.getId());

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

        // 3. 사용자 생성
        System.out.println("사용자 데이터 생성 중...");
        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            UserEntity user = createUser(i + 1);
            entityManager.persist(user);
            if ((i + 1) % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
        // 생성된 사용자 ID 목록을 가져옵니다.
        userIds.addAll(userRepository.findAll().stream().map(UserEntity::getId).toList());
        System.out.println(userCount + "명의 사용자 데이터 생성 완료.");

        // 4. 좋아요 생성 (인기 상품 위주로)
        System.out.println("좋아요 데이터 생성 중...");
        // 상위 1%의 인기 상품 ID
        int popularProductCount = Math.max(1, productCount / 100);
        List<Long> popularProductIds = entityManager.createQuery("SELECT p.id FROM ProductEntity p ORDER BY p.id ASC", Long.class)
                .setMaxResults(popularProductCount)
                .getResultList();

        int likeCount = 0;
        for (Long userId : userIds) {
            int likesPerUser = 5 + RANDOM.nextInt(26); // 사용자당 5~30개의 좋아요
            Collections.shuffle(popularProductIds);
            List<Long> productsToLike = popularProductIds.stream().limit(likesPerUser).toList();

            for (Long productId : productsToLike) {
                UserEntity userReference = entityManager.getReference(UserEntity.class, userId);
                ProductEntity productReference = entityManager.getReference(ProductEntity.class, productId);
                LikeEntity like = LikeEntity.create(userReference, productReference);
                entityManager.persist(like);
                likeCount++;

                if (likeCount % batchSize == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
        }
        entityManager.flush();
        entityManager.clear();
        System.out.println("총 " + likeCount + "개의 좋아요 데이터 생성이 완료되었습니다.");
    }

    private ProductEntity createProduct(int productSequence, BrandEntity brand) {
        String productName = "Product-" + productSequence;
        int price = (100 + RANDOM.nextInt(900)) * 100;
        int stock = RANDOM.nextInt(100);
        return ProductEntity.create(productName, price, stock, brand);
    }

    private UserEntity createUser(int userSequence) {
        String username = "user" + String.format("%04d", userSequence);
        String email = username + "@test.com";
        UserGender gender = RANDOM.nextBoolean() ? UserGender.M : UserGender.F;
        LocalDate birth = LocalDate.of(2000, 1, 1);
        return UserEntity.create(username, email, gender, birth);
    }
}

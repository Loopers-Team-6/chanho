package com.loopers.application.product;

import com.loopers.application.ProductFacade;
import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.product.ProductV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ProductFacadeTest {

    @Autowired
    private ProductFacade productFacade;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private UserEntity user1;
    private UserEntity user2;
    private ProductEntity product1;
    private ProductEntity product2;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        user1 = userRepository.save(UserEntity.create("user1", "user1@test.com", UserGender.MALE, LocalDate.now().minusYears(20)));
        user2 = userRepository.save(UserEntity.create("user2", "user2@test.com", UserGender.FEMALE, LocalDate.now().minusYears(30)));

        BrandEntity brand = brandRepository.save(BrandEntity.create("나이키"));
        product1 = productRepository.save(ProductEntity.create("신발", 130000, 10, brand));
        product2 = productRepository.save(ProductEntity.create("가방", 170000, 5, brand));
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품 상세 조회 시")
    @Nested
    class GetProduct {

        @DisplayName("상품 정보, 브랜드명, 전체 좋아요 수, 사용자의 좋아요 여부를 올바르게 반환한다")
        @Test
        void returnsProductDetailsWithBrandNameLikesCount() {
            // arrange
            likeRepository.save(LikeEntity.create(user1, product1));
            likeRepository.save(LikeEntity.create(user2, product1));
            likeRepository.save(LikeEntity.create(user2, product2));

            ProductV1Dto.ProductInfo result = productFacade.getProduct(product1.getId(), user1.getId());

            // then
            assertThat(result.id()).isEqualTo(product1.getId());
            assertThat(result.name()).isEqualTo(product1.getName());
            assertThat(result.brandName()).isEqualTo(product1.getBrand().getBrandName());
            assertThat(result.likesCount()).isEqualTo(2);
            assertThat(result.isLiked()).isTrue();
        }

        @DisplayName("사용자가 좋아요 누르지 않은 상품이면 isLiked가 false로 반환된다")
        @Test
        void returnsIsLikedFalseWhenUserHasNotLiked() {
            // arrange
            likeRepository.save(LikeEntity.create(user1, product1));
            likeRepository.save(LikeEntity.create(user2, product1));
            likeRepository.save(LikeEntity.create(user2, product2));

            // act
            ProductV1Dto.ProductInfo result = productFacade.getProduct(product2.getId(), user1.getId());

            // then
            assertThat(result.id()).isEqualTo(product2.getId());
            assertThat(result.likesCount()).isEqualTo(1);
            assertThat(result.isLiked()).isFalse();
        }
    }

    @DisplayName("상품 목록 조회 시")
    @Nested
    class GetProducts {

        @DisplayName("각 상품의 정보와 함께 좋아요 수, 사용자의 좋아요 여부를 올바르게 반환한다")
        @Test
        void returnsProductListWithLikesCountAndIsLiked() {
            // arrange
            likeRepository.save(LikeEntity.create(user1, product1));
            likeRepository.save(LikeEntity.create(user2, product1));
            likeRepository.save(LikeEntity.create(user2, product2));
            Pageable pageable = PageRequest.of(0, 10);

            // act
            PageResponse<ProductV1Dto.ProductInfo> results = productFacade.getProducts(pageable, user1.getId());

            // assert
            assertThat(results.getTotalElements()).isEqualTo(2);

            ProductV1Dto.ProductInfo resultForProduct1 = results.getContent().stream()
                    .filter(p -> p.id().equals(product1.getId())).findFirst().orElseThrow();

            ProductV1Dto.ProductInfo resultForProduct2 = results.getContent().stream()
                    .filter(p -> p.id().equals(product2.getId())).findFirst().orElseThrow();

            // product1 검증 (좋아요 수: 2, user1이 좋아요 누름)
            assertThat(resultForProduct1.likesCount()).isEqualTo(2);
            assertThat(resultForProduct1.isLiked()).isTrue();

            // product2 검증 (좋아요 수: 1, user1이 좋아요 안 누름)
            assertThat(resultForProduct2.likesCount()).isEqualTo(1);
            assertThat(resultForProduct2.isLiked()).isFalse();
        }
    }
}

package com.loopers.domain.like;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.like.FakeLikeRepository;
import com.loopers.infrastructure.product.FakeProductRepository;
import com.loopers.infrastructure.user.FakeUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class LikeServiceTest {

    /*
     * 좋아요 서비스 테스트
     * - [o] 좋아요 등록 시 멱등적으로 동작한다.
     * - [o] 좋아요 취소 시 멱등적으로 동작한다.
     */

    private LikeService likeService;
    private LikeRepository likeRepository;
    private UserRepository userRepository;
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        this.likeRepository = new FakeLikeRepository();
        this.userRepository = new FakeUserRepository();
        this.productRepository = new FakeProductRepository();
        this.likeService = new LikeService(likeRepository, userRepository, productRepository);
    }

    @DisplayName("좋아요를 ")
    @Nested
    class LikeAndUnlike {

        @DisplayName("등록할 때, 이미 등록된 상품에 대해서는 멱등적으로 동작한다")
        @Test
        void likeProductIdempotently() {
            // arrange
            UserEntity user = UserEntity.create(
                    "user",
                    "test@test.com",
                    UserGender.M,
                    "2000-01-01"
            );
            ProductEntity product = ProductEntity.create(
                    "Test Product",
                    100,
                    10,
                    BrandEntity.create("나이키")
            );

            userRepository.save(user);
            productRepository.save(product);

            long countBefore = likeRepository.countAll();

            // act
            likeService.addLike(user.getId(), product.getId());
            likeService.addLike(user.getId(), product.getId());

            // assert
            assertThat(likeRepository.countAll()).isEqualTo(countBefore + 1);
        }

        @DisplayName("취소할 때, 이미 취소된 상품에 대해서는 멱등적으로 동작한다")
        @Test
        void unlikeProductIdempotently() {
            // arrange
            UserEntity user = UserEntity.create(
                    "user",
                    "test@test.com",
                    UserGender.M,
                    "2000-01-01"
            );
            ProductEntity product = ProductEntity.create(
                    "Test Product",
                    100,
                    10,
                    BrandEntity.create("나이키")
            );
            userRepository.save(user);
            productRepository.save(product);

            long userId = user.getId();
            long productId = product.getId();

            LikeEntity like = likeService.addLike(userId, productId);
            assertThat(like).isNotNull();
            likeService.removeLike(userId, productId);
            LikeEntity removedLike = likeRepository.findById(like.getId()).get();
            assertThat(removedLike).isNotNull();
            ZonedDateTime deletedAt = removedLike.getDeletedAt();

            // act
            likeService.removeLike(userId, productId);
            LikeEntity reRemovedLike = likeRepository.findById(like.getId()).get();
            assertThat(reRemovedLike).isNotNull();

            // assert
            assertThat(reRemovedLike.getDeletedAt()).isEqualTo(deletedAt);
        }
    }
}

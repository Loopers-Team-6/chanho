package com.loopers.domain.like;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import com.loopers.domain.user.UserRepository;
import com.loopers.event.domain.like.LikeChangedEvent;
import com.loopers.infrastructure.like.FakeLikeRepository;
import com.loopers.infrastructure.product.FakeProductRepository;
import com.loopers.infrastructure.user.FakeUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
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

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        this.likeRepository = new FakeLikeRepository();
        this.userRepository = new FakeUserRepository();
        this.productRepository = new FakeProductRepository();
        this.likeService = new LikeService(likeRepository, userRepository, productRepository, eventPublisher);
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
                    UserGender.MALE,
                    LocalDate.of(2000, 1, 1)
            );
            ProductEntity product = ProductEntity.create(
                    "Test Product",
                    100,
                    10,
                    BrandEntity.create("나이키")
            );

            userRepository.save(user);
            productRepository.save(product);

            long countBefore = likeRepository.count();
            Long userId = user.getId();
            Long productId = product.getId();
            // act
            likeService.addLike(userId, productId);
            likeService.addLike(userId, productId);

            // assert
            assertThat(likeRepository.count()).isEqualTo(countBefore + 1);
            assertThat(likeRepository.findByUserIdAndProductId(userId, productId)).isNotNull();
        }

        @DisplayName("취소할 때, 이미 취소된 상품에 대해서는 멱등적으로 동작한다")
        @Test
        void unlikeProductIdempotently() {
            // arrange
            UserEntity user = UserEntity.create(
                    "user",
                    "test@test.com",
                    UserGender.MALE,
                    LocalDate.of(2000, 1, 1)
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

        @DisplayName("새로운 좋아요를 등록하면 이벤트를 발행한다")
        @Test
        void publishesEvent_whenNewLikeIsAdded() {
            // arrange
            UserEntity user = userRepository.save(UserEntity.create(
                    "user",
                    "test@test.com",
                    UserGender.MALE,
                    LocalDate.of(2000, 1, 1)
            ));
            ProductEntity product = productRepository.save(ProductEntity.create(
                    "신발",
                    10000,
                    10,
                    BrandEntity.create("나이키")
            ));

            // act
            likeService.addLike(user.getId(), product.getId());

            // assert
            verify(eventPublisher).publishEvent(any(LikeChangedEvent.class));
        }

        @DisplayName("이미 좋아요를 누른 상태에서는 이벤트를 발행하지 않는다")
        @Test
        void doesNotPublishEvent_whenLikeAlreadyExists() {
            // arrange
            UserEntity user = userRepository.save(UserEntity.create(
                    "user",
                    "test@test.com",
                    UserGender.MALE,
                    LocalDate.of(2000, 1, 1)
            ));
            ProductEntity product = productRepository.save(ProductEntity.create(
                    "신발",
                    10000,
                    10,
                    BrandEntity.create("나이키")
            ));
            likeRepository.save(LikeEntity.create(user, product));

            // act
            likeService.addLike(user.getId(), product.getId());

            // assert
            verifyNoInteractions(eventPublisher);
        }
    }
}

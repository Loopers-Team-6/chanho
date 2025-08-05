package com.loopers.domain.like;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LikeEntityTest {

    /*
     * 좋아요 엔티티 테스트
     * - [o] 좋아요는 사용자와 상품을 참조해야 한다
     */

    @DisplayName("좋아요를 생성할 때, ")
    @Nested
    class CreateLike {

        @DisplayName("사용자와 상품이 유효하면, 좋아요 생성에 성공한다")
        @Test
        void createLikeEntityWithValidUserAndProduct() {
            // arrange
            UserEntity user = UserEntity.create(
                    "testuser",
                    "test@test.com",
                    UserGender.M,
                    LocalDate.of(2000, 1, 1)
            );
            ProductEntity product = ProductEntity.create(
                    "Test Product",
                    100,
                    10,
                    BrandEntity.create("나이키")
            );
            // act
            LikeEntity like = LikeEntity.create(user, product);
            // assert
            assertThat(like.getUser()).isEqualTo(user);
        }

        @DisplayName("사용자나 상품이 유효하지 않으면, 좋아요 생성에 실패한다")
        @Test
        void failedToCreateLike_whenUserOrProductIsInvalid() {
            // arrange
            UserEntity user = UserEntity.create(
                    "testuser",
                    "test@test.com",
                    UserGender.M,
                    LocalDate.of(2000, 1, 1)
            );
            ProductEntity product = ProductEntity.create(
                    "Test Product",
                    100,
                    10,
                    BrandEntity.create("나이키")
            );

            // act & assert
            assertThrows(IllegalArgumentException.class, () -> LikeEntity.create(null, product)); // 사용자 없음
            assertThrows(IllegalArgumentException.class, () -> LikeEntity.create(user, null)); // 상품 없음
            assertThrows(IllegalArgumentException.class, () -> LikeEntity.create(null, null)); // 사용자와 상품 모두 없음
        }
    }
}

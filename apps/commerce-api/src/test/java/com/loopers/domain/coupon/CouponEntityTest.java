package com.loopers.domain.coupon;

import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CouponEntityTest {

    /*
     * 쿠폰 엔티티 테스트
     * - [o]  쿠폰 생성 시 이름, 종류, 소유자 정보가 있어야 한다.
     * - [o]  쿠폰 종류는 정액 / 정률로 구분된다.
     *   - [o]  정액 쿠폰의 할인 금액은 0보다 커야 한다.
     *   - [o]  정률 쿠폰의 할인 비율은 0% 초과 100% 이하여야 한다.
     * - [o]  쿠폰은 최대 한번만 사용될 수 있다.
     */

    private UserEntity owner;

    @BeforeEach
    void setUp() {
        owner = UserEntity.create(
                "testuser",
                "test@test.com",
                UserGender.M,
                LocalDate.of(2000, 1, 1));
    }

    @DisplayName("쿠폰을 생성할 때, ")
    @Nested
    class CreateCoupon {

        @DisplayName("이름, 소유자, 종류, 할인율/액수 정보가 있어야 한다.")
        @Test
        void shouldHaveNameTypeAndOwner() {
            // arrange
            String name = "Test Coupon";
            long discountAmount = 1000L;

            // act
            CouponEntity coupon = CouponEntity.ofFixed(name, owner, discountAmount);

            // assert
            assertThat(coupon.getName()).isEqualTo(name);
            assertThat(coupon.getOwner()).isEqualTo(owner);
            assertThat(coupon.getDiscountAmount(BigDecimal.valueOf(10000L))).isEqualTo(BigDecimal.valueOf(discountAmount));
        }

        @DisplayName("정보가 누락되면 예외가 발생한다.")
        @Test
        void shouldThrowExceptionWhenMissingInfo() {
            // arrange
            String name = null;
            UserEntity owner = null;
            long discountAmount = 1000L;
            // act & assert
            assertThrows(IllegalArgumentException.class, () -> {
                CouponEntity.ofFixed(name, owner, discountAmount);
            });
        }

        @DisplayName("정액 쿠폰의 할인 금액은 0보다 커야 한다.")
        @ParameterizedTest
        @ValueSource(longs = {0L, -100L, -1000L})
        void fixedCouponDiscountAmountMustBePositive(long discountAmount) {
            // arrange
            String name = "Test Fixed Coupon";
            // act & assert
            assertThrows(IllegalArgumentException.class, () -> {
                CouponEntity.ofFixed(name, owner, discountAmount);
            });
        }

        @DisplayName("정률 쿠폰의 할인 비율은 0% 초과 100% 이하여야 한다.")
        @ParameterizedTest
        @ValueSource(longs = {0L, 101L, -50L, 1000L})
        void percentageCouponDiscountRateMustBeValid(long discountRate) {
            // arrange
            String name = "Test Percentage Coupon";
            // act & assert
            assertThrows(IllegalArgumentException.class, () -> {
                CouponEntity.ofPercentage(name, owner, discountRate);
            });
        }
    }

    @DisplayName("쿠폰의 할인 금액을 계산할 때, ")
    @Nested
    class CalculateDiscountAmount {

        @DisplayName("정액 쿠폰의 할인 금액이 원래 가격보다 작거나 같으면 해당 금액을 반환한다.")
        @Test
        void fixedCouponDiscountAmount() {
            // arrange
            String name = "Fixed Coupon";
            long discountAmount = 1000L;
            CouponEntity coupon = CouponEntity.ofFixed(name, owner, discountAmount);
            BigDecimal originalPrice = BigDecimal.valueOf(5000L);

            // act
            BigDecimal discount = coupon.getDiscountAmount(originalPrice);

            // assert
            assertThat(discount).isEqualTo(BigDecimal.valueOf(discountAmount));
        }

        @DisplayName("정액 쿠폰의 할인 금액이 원래 가격보다 크면 원래 가격을 반환한다.")
        @Test
        void fixedCouponDiscountAmountExceedsOriginalPrice() {
            // arrange
            String name = "Fixed Coupon";
            long discountAmount = 10000L;
            CouponEntity coupon = CouponEntity.ofFixed(name, owner, discountAmount);
            BigDecimal originalPrice = BigDecimal.valueOf(5000L);

            // act
            BigDecimal discount = coupon.getDiscountAmount(originalPrice);

            // assert
            assertThat(discount).isEqualTo(originalPrice);
        }


        @DisplayName("정률 쿠폰은 원래 가격에 할인 비율을 적용한다.")
        @Test
        void percentageCouponDiscountAmount() {
            // arrange
            String name = "Percentage Coupon";
            long discountRate = 20L; // 20%
            CouponEntity coupon = CouponEntity.ofPercentage(name, owner, discountRate);
            BigDecimal originalPrice = BigDecimal.valueOf(5000L);

            // act
            BigDecimal discount = coupon.getDiscountAmount(originalPrice);

            // assert
            BigDecimal expectedDiscount = originalPrice
                    .multiply(BigDecimal.valueOf(discountRate))
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            assertThat(discount).isEqualTo(expectedDiscount);
        }
    }

    @DisplayName("쿠폰을 사용할 때, ")
    @Nested
    class UseCoupon {

        @DisplayName("쿠폰은 최대 한번만 사용될 수 있다.")
        @Test
        void couponCanBeUsedOnce() {
            // arrange
            String name = "Single Use Coupon";
            long discountAmount = 500L;
            CouponEntity coupon = CouponEntity.ofFixed(name, owner, discountAmount);

            // act
            assertThat(coupon.isUsed()).isFalse();
            coupon.use();
            assertThat(coupon.isUsed()).isTrue();

            // assert
            assertThrows(IllegalStateException.class, coupon::use);
        }
    }
}

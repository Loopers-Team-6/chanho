package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(name = "coupons")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class CouponEntity extends BaseEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity owner;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;

    @Embedded
    private DiscountPolicy discountPolicy;

    private CouponEntity(String name, UserEntity owner, DiscountPolicy discountPolicy) {
        Validator.validateName(name);
        Validator.validateOwner(owner);
        Validator.validateDiscountPolicy(discountPolicy);
        this.name = name;
        this.owner = owner;
        this.discountPolicy = discountPolicy;
    }

    public static CouponEntity ofFixed(String name, UserEntity owner, long value) {
        return new CouponEntity(name, owner, DiscountPolicy.ofFixed(value));
    }

    public static CouponEntity ofPercentage(String name, UserEntity owner, long rate) {
        return new CouponEntity(name, owner, DiscountPolicy.ofPercentage(rate));
    }

    public BigDecimal getDiscountAmount(BigDecimal originalPrice) {
        return discountPolicy.getDiscountAmount(originalPrice);
    }

    public void use() {
        if (isUsed) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        isUsed = true;
    }

    public void validateAvailability(Long userId) {
        if (isUsed) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        if (!owner.getId().equals(userId)) {
            throw new IllegalArgumentException("쿠폰 소유자와 요청한 사용자의 ID가 일치하지 않습니다.");
        }
    }

    static class Validator {
        static void validateName(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("쿠폰 이름은 필수입니다.");
            }
        }

        static void validateOwner(UserEntity owner) {
            if (owner == null) {
                throw new IllegalArgumentException("쿠폰 소유자는 필수입니다.");
            }
        }

        static void validateDiscountPolicy(DiscountPolicy discountPolicy) {
            if (discountPolicy == null) {
                throw new IllegalArgumentException("쿠폰 할인 정책은 필수입니다.");
            }
        }
    }

    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class DiscountPolicy {
        @Enumerated(EnumType.STRING)
        @Column(name = "coupon_type", nullable = false)
        private CouponType type;
        @Column(name = "discount_value", nullable = false)
        private long value;

        private static DiscountPolicy ofFixed(long amount) {
            if (amount <= 0) {
                throw new IllegalArgumentException("정액 쿠폰의 할인 금액은 0보다 커야 합니다.");
            }
            return new DiscountPolicy(CouponType.FIXED, amount);
        }

        private static DiscountPolicy ofPercentage(long rate) {
            if (rate <= 0 || rate > 100) {
                throw new IllegalArgumentException("정률 쿠폰의 할인 비율은 0% 초과 100% 이하여야 합니다.");
            }
            return new DiscountPolicy(CouponType.PERCENTAGE, rate);
        }

        public BigDecimal getDiscountAmount(BigDecimal originalPrice) {
            if (type == CouponType.FIXED) {
                return originalPrice.min(BigDecimal.valueOf(value));
            }

            if (type == CouponType.PERCENTAGE) {
                BigDecimal rate = BigDecimal.valueOf(value);
                BigDecimal hundred = new BigDecimal("100");

                return originalPrice.multiply(rate)
                        .divide(hundred, 0, RoundingMode.HALF_UP);
            }

            return BigDecimal.ZERO;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            DiscountPolicy that = (DiscountPolicy) obj;
            return value == that.value && type == that.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, value);
        }
    }
}

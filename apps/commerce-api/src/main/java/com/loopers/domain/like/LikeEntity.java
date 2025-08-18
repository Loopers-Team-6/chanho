package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_likes_user_product",
                columnNames = {"user_id", "product_id"}
        ),
        indexes = {
                @Index(name = "idx_likes_product_id", columnList = "product_id")
        }
)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class LikeEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private ProductEntity product;

    private LikeEntity(UserEntity user, ProductEntity product) {
        if (user == null) {
            throw new IllegalArgumentException("사용자 정보가 필요합니다.");
        }
        if (product == null) {
            throw new IllegalArgumentException("상품 정보가 필요합니다.");
        }

        this.user = user;
        this.product = product;
    }

    public static LikeEntity create(UserEntity user, ProductEntity product) {
        return new LikeEntity(user, product);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LikeEntity that)) return false;

        if (!user.getId().equals(that.user.getId())) return false;
        return product.getId().equals(that.product.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(user.getId(), product.getId());
    }
}

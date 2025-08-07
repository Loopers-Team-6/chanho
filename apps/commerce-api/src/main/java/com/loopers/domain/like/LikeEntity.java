package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "likes")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class LikeEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
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
}

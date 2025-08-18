package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.BrandEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_products_like_count", columnList = "like_count")
        }
)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class ProductEntity extends BaseEntity {

    @Column(nullable = false, name = "name", length = 50)
    private String name;

    @Column(nullable = false, name = "price", precision = 10)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false, name = "stock")
    private int stock;

    @ManyToOne
    @JoinColumn(
            name = "brand_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private BrandEntity brand;

    @Column(nullable = false, name = "like_count", columnDefinition = "bigint default 0")
    private long likeCount;

    @Version
    Long version;

    private ProductEntity(String productName, int price, int initialStock, BrandEntity brand) {
        if (productName == null || productName.isBlank() || price <= 0 || initialStock < 0 || brand == null) {
            throw new IllegalArgumentException("모든 필드는 유효해야 합니다.");
        }
        this.name = productName;
        this.price = BigDecimal.valueOf(price);
        this.stock = initialStock;
        this.brand = brand;
    }

    public static ProductEntity create(String productName, int price, int initialStock, BrandEntity brand) {
        return new ProductEntity(productName, price, initialStock, brand);
    }

    public void decreaseStock(int quantityToReduce) {
        if (quantityToReduce <= 0) {
            throw new IllegalArgumentException("차감할 수량은 0보다 커야 합니다.");
        }
        if (this.stock < quantityToReduce) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.stock -= quantityToReduce;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount <= 0) {
            return;
        }
        this.likeCount--;
    }

}

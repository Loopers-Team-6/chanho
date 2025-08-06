package com.loopers.interfaces.api.product;

import com.loopers.domain.product.ProductEntity;

import java.math.BigDecimal;

public class ProductV1Dto {
    public record ProductInfo(
            Long id,
            String name,
            BigDecimal price,
            int stock,
            String brandName,   // 브랜드명 추가
            long likesCount,    // 좋아요 수 추가
            boolean isLiked
    ) {
        public static ProductInfo of(ProductEntity product, long likesCount, boolean isLiked) {
            return new ProductInfo(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getStock(),
                    product.getBrand().getBrandName(),
                    likesCount,
                    isLiked
            );
        }
    }
}

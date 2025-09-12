package com.loopers.interfaces.api.ranking;

import com.loopers.domain.product.ProductEntity;

import java.math.BigDecimal;

public class RankingV1Dto {

    public record ProductRankResponse(
            long rank,
            double score,
            Long productId,
            String productName,
            String brandName,
            BigDecimal price
    ) {

        public static ProductRankResponse of(long rank, double score, ProductEntity product) {
            return new ProductRankResponse(
                    rank,
                    score,
                    product.getId(),
                    product.getName(),
                    product.getBrand()
                            .getBrandName(),
                    product.getPrice()
            );
        }
    }
}

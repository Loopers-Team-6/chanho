package com.loopers.domain.product;

public class ProductCommand {

    public record StockDecrease(
            long productId,
            int quantity
    ) {
        public StockDecrease {
            if (productId <= 0 || quantity <= 0) {
                throw new IllegalArgumentException("상품 ID와 수량은 유효해야 합니다.");
            }
        }
    }
}

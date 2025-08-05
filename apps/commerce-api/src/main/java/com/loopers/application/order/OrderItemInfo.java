package com.loopers.application.order;

import java.math.BigDecimal;

public record OrderItemInfo(
        Long productId,
        String productName,
        BigDecimal price,
        int quantity
) {
    public OrderItemInfo {
        validate(productId, productName, price, quantity);
    }

    private static void validate(Long productId, String productName, BigDecimal price, int quantity) {
        if (productId == null || productName == null || productName.isBlank() || price == null) {
            throw new IllegalArgumentException("상품 정보가 유효하지 않습니다");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("상품 가격은 0 이상이어야 합니다");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("상품 수량은 1 이상이어야 합니다");
        }
    }

}

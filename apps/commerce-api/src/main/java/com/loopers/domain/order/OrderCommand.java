package com.loopers.domain.order;

import java.util.List;

public class OrderCommand {

    public record Place(
            Long userId,
            List<OrderItemDetail> items
    ) {
        public Place {
            if (userId == null || items == null || items.isEmpty()) {
                throw new IllegalArgumentException("사용자 ID와 주문 항목은 유효해야 합니다");
            }
        }
    }

    public record OrderItemDetail(
            Long productId,
            int quantity
    ) {
        public OrderItemDetail {
            if (productId == null) {
                throw new IllegalArgumentException("주문 항목 정보가 유효하지 않습니다");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("상품 수량은 1 이상이어야 합니다");
            }
        }
    }
}

package com.loopers.domain.order;

import java.util.List;

public class OrderCommand {

    public record Place(
            Long userId,
            List<OrderItemDetail> items,
            Long couponId
    ) {
        public Place {
            Validator.validateUserId(userId);
            Validator.validateItems(items);
        }

        public static Place withoutCoupon(Long userId, List<OrderItemDetail> items) {
            return new Place(userId, items, null);
        }

        public static Place withCoupon(Long userId, List<OrderItemDetail> items, Long couponId) {
            if (couponId == null || couponId <= 0) {
                throw new IllegalArgumentException("쿠폰 ID는 유효해야 합니다.");
            }
            return new Place(userId, items, couponId);
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

    static class Validator {
        public static void validateUserId(Long userId) {
            if (userId == null || userId <= 0) {
                throw new IllegalArgumentException("사용자 ID는 유효해야 합니다.");
            }
        }

        public static void validateItems(List<OrderItemDetail> items) {
            if (items == null || items.isEmpty()) {
                throw new IllegalArgumentException("주문 항목은 비어있을 수 없습니다");
            }

            for (OrderItemDetail item : items) {
                if (item == null || item.productId() == null || item.quantity() <= 0) {
                    throw new IllegalArgumentException("주문 항목 정보가 유효하지 않습니다");
                }
            }
        }
    }
}

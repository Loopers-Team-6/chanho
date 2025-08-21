package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderInfo;
import com.loopers.application.order.OrderItemInfo;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.List;
import java.util.Objects;

public class OrderV1Dto {

    public record OrderRequest(
            List<OrderItemRequest> items,
            PaymentMethod paymentMethod,
            Long couponId
    ) {
        public OrderRequest {
            if (items == null || items.isEmpty() || items.stream().anyMatch(Objects::isNull)) {
                throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목은 비어있을 수 없습니다");
            }
            if (paymentMethod == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "결제 방법은 필수입니다");
            }
            if (couponId != null && couponId <= 0) {
                throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 ID가 올바르지 않습니다");
            }
        }

        public record OrderItemRequest(
                Long productId,
                int quantity
        ) {
            public OrderItemRequest {
                if (productId == null) {
                    throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다");
                }
                if (quantity <= 0) {
                    throw new CoreException(ErrorType.BAD_REQUEST, "상품 수량은 1 이상이어야 합니다");
                }
            }
        }
    }

    public record OrderResponse(
            Long orderId,
            OrderStatus status,
            PaymentMethod paymentMethod,
            String totalPrice,
            List<OrderItemResponse> items
    ) {
        public static OrderResponse from(OrderInfo orderInfo) {
            if (orderInfo == null) {
                throw new IllegalStateException("주문 정보는 null일 수 없습니다. 서버 로직을 확인해주세요.");
            }

            return new OrderResponse(
                    orderInfo.getOrderId(),
                    orderInfo.getOrderStatus(),
                    orderInfo.getPaymentMethod(),
                    orderInfo.getTotalPrice().toPlainString(),
                    orderInfo.getOrderItems().stream()
                            .map(OrderItemResponse::from)
                            .toList()
            );
        }

        public OrderResponse {
            if (orderId == null || orderId <= 0) {
                throw new IllegalStateException("주문 ID가 유효하지 않습니다: " + orderId);
            }
            if (status == null) {
                throw new IllegalStateException("주문 상태가 유효하지 않습니다: " + totalPrice);
            }
            if (paymentMethod == null) {
                throw new IllegalStateException("결제 방법이 유효하지 않습니다");
            }
            if (totalPrice == null || totalPrice.isBlank()) {
                throw new IllegalStateException("총 가격은 비어있을 수 없습니다");
            }
            if (items == null || items.isEmpty() || items.stream().anyMatch(Objects::isNull)) {
                throw new IllegalStateException("주문 항목은 비어있을 수 없습니다");
            }
        }

        public record OrderItemResponse(
                Long productId,
                String productName,
                int quantity,
                String price
        ) {
            public static OrderItemResponse from(OrderItemInfo item) {
                if (item == null) {
                    throw new IllegalStateException("주문 항목은 null일 수 없습니다. 서버 로직을 확인해주세요.");
                }

                return new OrderItemResponse(
                        item.productId(),
                        item.productName(),
                        item.quantity(),
                        item.price().toPlainString()
                );
            }

            public OrderItemResponse {
                if (productId == null) {
                    throw new IllegalStateException("상품 ID가 유효하지 않습니다.");
                }
                if (quantity <= 0) {
                    throw new IllegalStateException("상품 수량이 유효하지 않습니다: " + quantity);
                }
                if (productName == null || productName.isBlank()) {
                    throw new IllegalStateException("상품 이름은 비어있을 수 없습니다");
                }
                if (price == null || price.isBlank()) {
                    throw new IllegalStateException("가격은 비어있을 수 없습니다");
                }
            }
        }
    }
}

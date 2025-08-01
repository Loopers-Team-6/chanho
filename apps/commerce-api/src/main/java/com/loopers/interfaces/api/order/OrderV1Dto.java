package com.loopers.interfaces.api.order;

import com.loopers.domain.payment.PaymentMethod;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.List;
import java.util.Objects;

public class OrderV1Dto {
    public record OrderRequest(
            List<OrderItemRequest> items,
            PaymentMethod paymentMethod
    ) {
        public OrderRequest {
            if (items == null || items.isEmpty() || items.stream().anyMatch(Objects::isNull)) {
                throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목은 비어있을 수 없습니다");
            }
            if (paymentMethod == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "결제 방법은 필수입니다");
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
}

package com.loopers.application.order;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.PaymentMethod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class OrderInfo {

    private final Long orderId;
    private final Long userId;
    private final OrderStatus orderStatus;
    private final PaymentMethod paymentMethod;
    private final List<OrderItemInfo> orderItems;
    private final BigDecimal totalPrice;

    public static OrderInfo from(OrderEntity orderEntity, PaymentMethod paymentMethod) {
        if (orderEntity == null) {
            throw new IllegalStateException("주문 정보는 null일 수 없습니다. 서버 로직을 확인해주세요.");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("결제 방법은 null일 수 없습니다.");
        }

        return new OrderInfo(
                orderEntity.getId(),
                orderEntity.getUser().getId(),
                orderEntity.getStatus(),
                paymentMethod,
                orderEntity.getItems().stream()
                        .map(OrderItemInfo::from)
                        .toList(),
                orderEntity.getOriginalPrice()
        );
    }
}

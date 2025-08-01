package com.loopers.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class OrderInfo {

    private final Long orderId;
    private final Long userId;
    private final String orderStatus;
    private final BigDecimal totalPrice;

    public static OrderInfo from(OrderEntity orderEntity) {
        return new OrderInfo(
                orderEntity.getId(),
                orderEntity.getUser().getId(),
                orderEntity.getStatus().name(),
                orderEntity.getTotalPrice()
        );
    }
}

package com.loopers.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class OrderInfo {

    private final Long orderId;
    private final Long userId;
    private final String orderStatus;

    public static OrderInfo from(OrderEntity orderEntity) {
        return new OrderInfo(
                orderEntity.getId(),
                orderEntity.getUser().getId(),
                orderEntity.getStatus().name()
        );
    }
}

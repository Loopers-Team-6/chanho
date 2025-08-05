package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.UserEntity;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
public class OrderEntity extends BaseEntity {

    private UserEntity user;
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private OrderStatus status = OrderStatus.PENDING;

    private OrderEntity(UserEntity user) {
        this.user = user;
    }

    public static OrderEntity create(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("사용자 정보는 유효해야 합니다.");
        }

        return new OrderEntity(user);
    }

    public void addOrderItem(Long productId, String productName, BigDecimal price, int quantity) {
        if (productId == null || productName == null || productName.isBlank() || price == null) {
            throw new IllegalArgumentException("주문 항목 정보가 유효하지 않습니다");
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("상품 가격은 0 이상이어야 합니다");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("상품 수량은 1 이상이어야 합니다");
        }

        OrderItem newItem = OrderItem.create(this, productId, productName, price, quantity);
        items.add(newItem);
        totalPrice = totalPrice.add(newItem.getTotalPrice());
    }

    public List<Long> getProductIds() {
        if (items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .map(OrderItem::getProductId)
                .toList();
    }

    public void complete() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("오직 대기 중인 주문만 완료할 수 있습니다.");
        }
        status = OrderStatus.COMPLETED;
    }

    public void cancel() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("오직 대기 중인 주문만 취소할 수 있습니다.");
        }
        status = OrderStatus.CANCELLED;
    }

    public void fail() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("오직 대기 중인 주문만 취소할 수 있습니다.");
        }
        status = OrderStatus.FAILED;
    }

    enum OrderStatus {
        PENDING,
        COMPLETED,
        CANCELLED,
        FAILED;
    }
}

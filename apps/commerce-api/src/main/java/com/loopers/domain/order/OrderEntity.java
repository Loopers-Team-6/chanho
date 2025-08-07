package com.loopers.domain.order;

import com.loopers.application.order.OrderItemInfo;
import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class OrderEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();
    @Column(name = "total_price", precision = 10, nullable = false)
    private BigDecimal totalPrice = BigDecimal.ZERO;
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;
    @Column(name = "applied_coupon_id")
    private Long appliedCouponId = null;
    @Column(name = "discount_amount", precision = 10, nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private OrderEntity(UserEntity user) {
        this.user = user;
    }

    public static OrderEntity create(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("사용자 정보는 유효해야 합니다.");
        }

        return new OrderEntity(user);
    }

    public void addOrderItem(OrderItemInfo item) {
        if (item == null) {
            throw new IllegalArgumentException("주문 항목 정보는 null일 수 없습니다.");
        }

        OrderItem newItem = OrderItem.create(this, item.productId(), item.productName(), item.price(), item.quantity());
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
            throw new IllegalStateException("오직 대기 중인 주문만 실패처리할 수 있습니다.");
        }
        status = OrderStatus.FAILED;
    }

    public void applyDiscount(Long couponId, BigDecimal amount) {
        this.appliedCouponId = couponId;
        this.discountAmount = amount;
    }

    public BigDecimal getFinalPrice() {
        return getTotalPrice().subtract(this.discountAmount);
    }

    enum OrderStatus {
        PENDING,
        COMPLETED,
        CANCELLED,
        FAILED;
    }
}

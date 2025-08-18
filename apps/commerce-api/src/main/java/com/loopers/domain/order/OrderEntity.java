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
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private UserEntity user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "original_price", precision = 10, nullable = false)
    private BigDecimal originalPrice = BigDecimal.ZERO;

    @Column(name = "final_price", precision = 10, nullable = false)
    private BigDecimal finalPrice = BigDecimal.ZERO;

    @Column(name = "applied_coupon_id")
    private Long appliedCouponId = null;

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
        originalPrice = originalPrice.add(newItem.getTotalPrice());
        finalPrice = originalPrice;
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

    public void applyDiscount(Long couponId, BigDecimal discountAmount) {
        this.appliedCouponId = couponId;

        if (discountAmount.compareTo(originalPrice) > 0) {
            this.finalPrice = BigDecimal.ZERO;
            return;
        }

        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("할인 금액은 0보다 커야 합니다.");
        }

        this.finalPrice = originalPrice.subtract(discountAmount);
    }

    enum OrderStatus {
        PENDING,
        COMPLETED,
        CANCELLED,
        FAILED;
    }
}

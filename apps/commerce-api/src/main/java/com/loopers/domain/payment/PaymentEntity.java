package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.OrderEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private OrderEntity order;

    @Column(name = "method", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Column(name = "amount", precision = 10, nullable = false)
    private BigDecimal amount;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private PaymentEntity(OrderEntity order, PaymentMethod method) {
        this.order = order;
        this.method = method;
        this.amount = order.getFinalPrice();
        this.status = PaymentStatus.PENDING;
    }

    public static PaymentEntity create(OrderEntity order, PaymentMethod method) {
        return new PaymentEntity(order, method);
    }

    public void complete() {
        changePaymentStatus(PaymentStatus.COMPLETED);
    }

    public void cancel() {
        changePaymentStatus(PaymentStatus.CANCELED);
    }

    public void fail() {
        changePaymentStatus(PaymentStatus.FAILED);
    }

    private void changePaymentStatus(PaymentStatus newStatus) {
        if (this.status == newStatus) {
            return;
        }

        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("결제 상태가 PENDING이 아닙니다. 현재 상태: " + this.status);
        }

        this.status = newStatus;
    }

}

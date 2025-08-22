package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "method", discriminatorType = DiscriminatorType.STRING)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PaymentEntity extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "method", nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Column(name = "amount", precision = 10, nullable = false)
    private BigDecimal amount;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    protected PaymentEntity(Long orderId, PaymentMethod method, BigDecimal amout) {
        this.orderId = orderId;
        this.method = method;
        this.amount = amout;
        this.status = PaymentStatus.PENDING;
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

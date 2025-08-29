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

    @Column(name = "order_id", nullable = false, unique = true)
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
        this.status = PaymentStatus.CREATED;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }

    public boolean isSuccess() {
        return this.status == PaymentStatus.SUCCESS;
    }

    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }

    public boolean isCanceled() {
        return this.status == PaymentStatus.CANCELED;
    }

    public void markAsPending() {
        updatePaymentStatus(PaymentStatus.PENDING);
    }

    public void markAsSuccess() {
        updatePaymentStatus(PaymentStatus.SUCCESS);
    }

    public void markAsFailed() {
        updatePaymentStatus(PaymentStatus.FAILED);
    }

    public void markAsCanceled() {
        updatePaymentStatus(PaymentStatus.CANCELED);
    }

    public void updatePaymentStatus(PaymentStatus newStatus) {
        if (this.status == newStatus) {
            return;
        }

        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException("결제 상태를 변경할 수 없습니다. 현재 상태: " + this.status + ", 변경하려는 상태: " + newStatus);
        }

        this.status = newStatus;
    }
}

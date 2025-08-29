package com.loopers.domain.payment;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "card_payments")
@DiscriminatorValue("CARD")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardPaymentEntity extends PaymentEntity {

    @Column(name = "transaction_key", unique = true)
    private String transactionKey;

    private CardPaymentEntity(Long orderId, BigDecimal amount, String transactionKey) {
        super(orderId, PaymentMethod.CARD, amount);
        this.transactionKey = transactionKey;
    }

    public static PaymentEntity create(Long orderId, BigDecimal amount, String transactionId) {
        return new CardPaymentEntity(orderId, amount, transactionId);
    }

    public void updateTransactionInfo(String transactionKey, PaymentStatus status) {
        if (transactionKey == null || transactionKey.isBlank()) {
            throw new IllegalArgumentException("트랜잭션 키가 올바르지 않습니다.");
        }
        this.transactionKey = transactionKey;
        updatePaymentStatus(status);
    }
}

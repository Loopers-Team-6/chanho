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

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    private CardPaymentEntity(Long orderId, BigDecimal amount, String transactionId) {
        super(orderId, PaymentMethod.CARD, amount);
        this.transactionId = transactionId;
    }

    public static PaymentEntity create(Long orderId, BigDecimal amount, String transactionId) {
        return new CardPaymentEntity(orderId, amount, transactionId);
    }
}

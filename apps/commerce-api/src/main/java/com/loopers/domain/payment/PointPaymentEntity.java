package com.loopers.domain.payment;

import com.loopers.domain.order.OrderEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "point_payments")
@DiscriminatorValue("POINT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointPaymentEntity extends PaymentEntity {

    private PointPaymentEntity(Long orderId, BigDecimal amount) {
        super(orderId, PaymentMethod.POINT, amount);
    }

    public static PaymentEntity create(Long orderId, BigDecimal amount) {
        return new PointPaymentEntity(orderId, amount);
    }

    public static PaymentEntity create(OrderEntity order) {
        return new PointPaymentEntity(order.getId(), order.getFinalPrice());
    }
}

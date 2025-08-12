package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;
    @Column(name = "price", precision = 10, nullable = false)
    private BigDecimal price;
    @Column(name = "quantity", nullable = false)
    private int quantity;

    private OrderItem(OrderEntity order, Long productId, String productName, BigDecimal price, int quantity) {
        this.order = order;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public static OrderItem create(OrderEntity order, Long productId, String productName, BigDecimal price, int quantity) {
        return new OrderItem(order, productId, productName, price, quantity);
    }

    public BigDecimal getTotalPrice() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

}

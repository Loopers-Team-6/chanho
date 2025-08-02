package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderItem extends BaseEntity {

    private OrderEntity order;
    private Long productId;
    private String productName;
    private BigDecimal price;
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

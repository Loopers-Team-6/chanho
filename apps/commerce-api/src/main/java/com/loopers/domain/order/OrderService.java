package com.loopers.domain.order;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserEntity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PointRepository pointRepository;

    public OrderInfo placeOrder(UserEntity user, OrderCommand.Place command) {
        Validator.validateUser(user);
        Validator.validatePlaceOrderCommand(command);

        OrderEntity order = OrderEntity.create(user);

        for (OrderCommand.OrderItemDetail item : command.items()) {
            ProductEntity product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다: " + item.productId()));
            product.decreaseStock(item.quantity());
            order.addOrderItem(product.getId(), product.getName(), product.getPrice(), item.quantity());
            PointEntity point = pointRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자의 포인트 정보가 존재하지 않습니다: " + user.getId()));
            point.pay(item.quantity() * product.getPrice().intValue());
        }

        order.completeOrder();

        OrderEntity saved = orderRepository.save(order);
        if (saved == null) {
            throw new IllegalStateException("주문 저장에 실패했습니다.");
        }

        return OrderInfo.from(saved);
    }

    static class Validator {
        public static void validatePlaceOrderCommand(OrderCommand.Place command) {
            if (command == null) {
                throw new IllegalArgumentException("주문 명령은 유효해야 합니다.");
            }
            if (command.userId() == null || command.items() == null || command.items().isEmpty()) {
                throw new IllegalArgumentException("사용자 ID와 주문 항목은 유효해야 합니다.");
            }
            for (OrderCommand.OrderItemDetail item : command.items()) {
                if (item == null || item.productId() == null || item.quantity() <= 0) {
                    throw new IllegalArgumentException("주문 항목 정보가 유효하지 않습니다.");
                }
            }
        }

        public static void validateUser(UserEntity user) {
            if (user == null || user.getId() == null) {
                throw new IllegalArgumentException("사용자 정보는 유효해야 합니다.");
            }
        }
    }
}

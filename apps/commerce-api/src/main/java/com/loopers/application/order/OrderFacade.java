package com.loopers.application.order;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OrderFacade {

    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;
    private final PointService pointService;

    @Transactional
    public OrderInfo placeOrder(long userId, OrderCommand.Place command) {
        Validator.validatePlaceOrderCommand(command);

        UserEntity user = userService.findById(userId);

        Map<ProductEntity, Integer> productQuantities = getProductQuantities(command.items());

        decreaseStock(productQuantities);

        BigDecimal totalPrice = orderService.calculateTotalPrice(productQuantities);

        pointService.deductPoints(user.getId(), totalPrice);

        OrderEntity order = orderService.createOrder(user);

        addOrderItems(order, productQuantities);

        order.completeOrder();

        OrderEntity saved = orderService.save(order);

        return OrderInfo.from(saved);
    }

    private Map<ProductEntity, Integer> getProductQuantities(List<OrderCommand.OrderItemDetail> items) {
        Map<Long, Integer> productIds = items.stream()
                .collect(Collectors.toMap(
                        OrderCommand.OrderItemDetail::productId,
                        OrderCommand.OrderItemDetail::quantity,
                        Integer::sum
                ));
        List<ProductEntity> productsToOrder = productService.findAllById(productIds.keySet().stream().toList());

        return productsToOrder.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        product -> productIds.get(product.getId())
                ));
    }

    private void decreaseStock(Map<ProductEntity, Integer> productQuantities) {
        productQuantities.forEach(ProductEntity::decreaseStock);
    }

    private void addOrderItems(OrderEntity order, Map<ProductEntity, Integer> productQuantities) {
        productQuantities.forEach((product, quantity) ->
                order.addOrderItem(product.getId(), product.getName(), product.getPrice(), quantity));
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
    }
}

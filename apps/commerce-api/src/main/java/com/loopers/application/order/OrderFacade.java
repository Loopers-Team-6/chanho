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

import java.util.Map;

@RequiredArgsConstructor
public class OrderFacade {

    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;
    private final PointService pointService;

    @Transactional
    public OrderInfo placeOrder(OrderCommand.Place command) {
        if (command == null) {
            throw new IllegalArgumentException("주문 명령은 null일 수 없습니다.");
        }
        UserEntity user = userService.findById(command.userId());

        Map<ProductEntity, Integer> productQuantities = productService.getProductQuantities(command.items());
        productQuantities.forEach(ProductEntity::decreaseStock);

        OrderEntity order = orderService.save(OrderEntity.create(user));
        orderService.addOrderItems(order, productQuantities);

        pointService.deductPoints(user.getId(), order.getTotalPrice());

        order.complete();
        OrderEntity saved = orderService.save(order);

        return OrderInfo.from(saved);
    }

}

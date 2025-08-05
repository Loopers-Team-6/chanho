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

import java.util.List;

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

        // 1. 상품 조회 및 재고 차감
        List<OrderItemInfo> orederItems = prepareAndDecreaseStocks(command.items());
        
        // 2. 주문 생성
        OrderEntity order = orderService.save(OrderEntity.create(user));
        orederItems.forEach(order::addOrderItem);

        // 3. 포인트 차감
        pointService.deductPoints(user.getId(), order.getTotalPrice());

        // 4. 주문 완료
        order.complete();
        OrderEntity saved = orderService.save(order);

        return OrderInfo.from(saved);
    }

    private List<OrderItemInfo> prepareAndDecreaseStocks(List<OrderCommand.OrderItemDetail> items) {
        return items.stream()
                .map(item -> {
                    ProductEntity product = productService.findById(item.productId());
                    product.decreaseStock(item.quantity());

                    return new OrderItemInfo(
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            item.quantity()
                    );
                })
                .toList();
    }

}

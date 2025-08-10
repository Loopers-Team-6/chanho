package com.loopers.application.order;

import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponService;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;
    private final PointService pointService;
    private final CouponService couponService;

    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    @Transactional
    public OrderInfo placeOrder(OrderCommand.Place command) {
        if (command == null) {
            throw new IllegalArgumentException("주문 명령은 null일 수 없습니다.");
        }
        UserEntity user = userService.findById(command.userId());

        // 1. 상품 조회 및 재고 차감
        List<OrderItemInfo> orderItems = prepareAndDecreaseStocks(command.items());

        // 2. 주문 생성
        OrderEntity order = orderService.save(OrderEntity.create(user));
        orderItems.forEach(order::addOrderItem);

        // 3. 쿠폰 적용 로직
        applyCoupon(order, command.couponId());

        // 4. 최종 가격으로 포인트 차감
        BigDecimal finalPrice = order.getFinalPrice();
        pointService.deductPoints(user.getId(), finalPrice);

        // 5. 주문 완료
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

    private void applyCoupon(OrderEntity order, Long couponId) {
        if (couponId == null) {
            return;
        }

        CouponEntity coupon = couponService.findById(couponId);
        coupon.validateAvailability(order.getUser().getId());

        BigDecimal originalPrice = order.getOriginalPrice();
        BigDecimal discountAmount = coupon.getDiscountAmount(originalPrice);

        coupon.use();
        couponService.save(coupon);
        order.applyDiscount(coupon.getId(), discountAmount);
    }
}

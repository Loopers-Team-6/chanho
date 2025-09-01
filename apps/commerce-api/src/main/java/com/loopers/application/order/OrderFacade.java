package com.loopers.application.order;

import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.event.OrderPlacedEvent;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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

    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;
    private final CouponService couponService;
    private final PaymentService paymentService;

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

        // 1. 상품 조회 및 재고 확인
        List<OrderItemInfo> orderItems = checkStocks(command.items());

        // 2. 주문 생성
        OrderEntity order = orderService.save(OrderEntity.create(user));
        orderItems.forEach(order::addOrderItem);

        // 3. 쿠폰 적용 로직
        applyCoupon(order, command.couponId());

        // 4. 주문 상태 업데이트
        OrderEntity saved = orderService.save(order);

        // 5. 주문생성 이벤트 발행
        eventPublisher.publishEvent(new OrderPlacedEvent(saved.getId(), command.paymentMethod(), saved.getFinalPrice(), orderItems));

        return OrderInfo.from(saved, command.paymentMethod());
    }

    private List<OrderItemInfo> checkStocks(List<OrderCommand.OrderItemDetail> items) {
        return items.stream()
                .map(item -> {
                    ProductEntity product = productService.findById(item.productId());
                    if (product.getStock() < item.quantity()) {
                        throw new IllegalArgumentException("재고가 부족합니다.");
                    }
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

    @Transactional(readOnly = true)
    public OrderInfo getOrder(OrderCommand.Find find) {
        UserEntity user = userService.findById(find.userId());
        OrderEntity order = orderService.findById(find.orderId());

        if (!order.getUser().getId().equals(user.getId())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "해당 주문은 사용자의 주문이 아닙니다.");
        }

        PaymentEntity payment = paymentService.findByOrderId(order.getId());

        return OrderInfo.from(order, payment.getMethod());
    }
}

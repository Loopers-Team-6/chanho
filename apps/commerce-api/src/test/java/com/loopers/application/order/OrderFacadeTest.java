package com.loopers.application.order;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.*;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.PointServiceImpl;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserServiceImpl;
import com.loopers.infrastructure.brand.FakeBrandRepository;
import com.loopers.infrastructure.coupon.FakeCouponRepository;
import com.loopers.infrastructure.order.FakeOrderRepository;
import com.loopers.infrastructure.point.FakePointRepository;
import com.loopers.infrastructure.product.FakeProductRepository;
import com.loopers.infrastructure.user.FakeUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderFacadeTest {

    /*
     * 주문 파사드 테스트
     *
     */

    private static final BigDecimal DEFAULT_POINT_AMOUNT = BigDecimal.valueOf(1000000L);

    private OrderFacade orderFacade;
    private UserEntity testUser;
    private PointEntity testPoint;
    private ProductEntity productA;
    private ProductEntity productB;

    private UserRepository userRepository;
    private BrandRepository brandRepository;
    private ProductRepository productRepository;
    private OrderRepository orderRepository;
    private PointRepository pointRepository;
    private CouponRepository couponRepository;

    @BeforeEach
    public void setUp() {
        userRepository = new FakeUserRepository();
        productRepository = new FakeProductRepository();
        brandRepository = new FakeBrandRepository();
        orderRepository = new FakeOrderRepository();
        pointRepository = new FakePointRepository();
        couponRepository = new FakeCouponRepository();
        orderFacade = new OrderFacade(
                new UserServiceImpl(userRepository),
                new OrderService(orderRepository),
                new ProductService(productRepository),
                new PointServiceImpl(pointRepository),
                new CouponService(couponRepository)
        );

        testUser = userRepository.save(UserEntity.create(
                "testUser",
                "test@email.com",
                UserGender.M,
                LocalDate.now().minusYears(20)));
        testPoint = pointRepository.save(new PointEntity(testUser));

        BrandEntity brand = brandRepository.save(BrandEntity.create("나이키"));
        productA = productRepository.save(ProductEntity.create("ProductA", 10000, 10, brand));
        productB = productRepository.save(ProductEntity.create("ProductB", 25000, 5, brand));
    }

    @DisplayName("주문을 생성할 때, ")
    @Nested
    class PlaceOrder {

        @DisplayName("사용자가 유효하지 않으면 예외가 발생한다")
        @Test
        void createOrderWithIntestUser() {
            // arrange
            OrderCommand.Place command = OrderCommand.Place.create(
                    999L,
                    List.of(
                            new OrderCommand.OrderItemDetail(productA.getId(), 2),
                            new OrderCommand.OrderItemDetail(productA.getId(), 1)
                    ),
                    PaymentMethod.POINT);

            // act & assert
            assertThrows(EntityNotFoundException.class, () -> orderFacade.placeOrder(command));
        }

        @DisplayName("주문 항목이 유효하지 않으면 예외가 발생한다")
        @Test
        void createOrderWithInvalidCommand() {
            // act & assert
            assertThrows(IllegalArgumentException.class, () -> orderFacade.placeOrder(null));
        }

        @DisplayName("주문 항목이 유효하면 주문을 생성한다")
        @Test
        void createOrderWithValidCommand() {
            // arrange
            testPoint.charge(DEFAULT_POINT_AMOUNT);
            pointRepository.save(testPoint);
            OrderCommand.Place command = OrderCommand.Place.create(
                    testUser.getId(),
                    List.of(
                            new OrderCommand.OrderItemDetail(productA.getId(), 2),
                            new OrderCommand.OrderItemDetail(productB.getId(), 1)
                    ),
                    PaymentMethod.POINT);

            // act
            orderFacade.placeOrder(command);

            // assert
            assertThat(orderRepository.findById(testUser.getId()))
                    .isPresent()
                    .get()
                    .extracting(OrderEntity::getUser)
                    .isEqualTo(testUser);
        }

        @DisplayName("주문 항목에 상품이 없으면 예외가 발생한다")
        @Test
        void createOrderWithEmptyItems() {
            // arrange
            Long userId = testUser.getId();
            // act & assert
            assertThrows(IllegalArgumentException.class, () -> OrderCommand.Place.create(userId, Arrays.asList(null, null), PaymentMethod.POINT));
        }

        @DisplayName("올바르게 주문이 생성되면, 재고가 차감된다")
        @Test
        void createOrderAndReduceStock() {
            // arrange
            testPoint.charge(DEFAULT_POINT_AMOUNT);
            pointRepository.save(testPoint);
            int stockA = productA.getStock();
            int stockB = productB.getStock();

            int quantityA = 2;
            int quantityB = 1;

            OrderCommand.Place command = OrderCommand.Place.create(
                    testUser.getId(),
                    List.of(
                            new OrderCommand.OrderItemDetail(productA.getId(), quantityA),
                            new OrderCommand.OrderItemDetail(productB.getId(), quantityB)
                    ),
                    PaymentMethod.POINT);

            // act
            orderFacade.placeOrder(command);

            // assert
            ProductEntity updatedProductA = productRepository.findById(productA.getId()).orElseThrow();
            ProductEntity updatedProductB = productRepository.findById(productB.getId()).orElseThrow();

            assertThat(updatedProductA.getStock()).isEqualTo(stockA - quantityA);
            assertThat(updatedProductB.getStock()).isEqualTo(stockB - quantityB);
        }

        @DisplayName("주문이 완료되면, 주문 상태가 COMPLETED로 변경된다")
        @Test
        void completeOrderStatusAfterPlacingOrder() {
            // arrange
            testPoint.charge(DEFAULT_POINT_AMOUNT);
            pointRepository.save(testPoint);
            OrderCommand.Place command = OrderCommand.Place.create(
                    testUser.getId(),
                    List.of(
                            new OrderCommand.OrderItemDetail(productA.getId(), 2),
                            new OrderCommand.OrderItemDetail(productB.getId(), 1)
                    ),
                    PaymentMethod.POINT);

            // act
            OrderInfo orderInfo = orderFacade.placeOrder(command);

            // assert
            assertThat(orderInfo.getOrderStatus()).isEqualTo("COMPLETED");
        }

        @DisplayName("주문이 완료되면 포인트가 차감된다")
        @Test
        void reducePointsAfterOrderCompletion() {
            // arrange
            testPoint.charge(DEFAULT_POINT_AMOUNT);
            pointRepository.save(testPoint);
            OrderCommand.Place command = OrderCommand.Place.create(
                    testUser.getId(),
                    List.of(
                            new OrderCommand.OrderItemDetail(productA.getId(), 2),
                            new OrderCommand.OrderItemDetail(productB.getId(), 1)
                    ),
                    PaymentMethod.POINT);
            BigDecimal totalPrice = productA.getPrice().multiply(BigDecimal.valueOf(2))
                    .add(productB.getPrice().multiply(BigDecimal.valueOf(1)));

            BigDecimal amountBeforeOrder = testPoint.getAmount();

            // act
            OrderInfo orderInfo = orderFacade.placeOrder(command);

            // assert
            assertThat(testPoint.getAmount()).isEqualTo(amountBeforeOrder.subtract(totalPrice));
            assertThat(orderInfo.getTotalPrice()).isEqualTo(totalPrice);
        }

        @DisplayName("포인트가 모자라면 주문이 실패한다")
        @Test
        void failOrderWhenInsufficientPoints() {
            // arrange
            pointRepository.save(testPoint);
            OrderCommand.Place command = OrderCommand.Place.create(
                    testUser.getId(),
                    List.of(
                            new OrderCommand.OrderItemDetail(productA.getId(), 2),
                            new OrderCommand.OrderItemDetail(productB.getId(), 1)
                    ),
                    PaymentMethod.POINT);
            BigDecimal totalPrice = productA.getPrice().multiply(BigDecimal.valueOf(2))
                    .add(productB.getPrice().multiply(BigDecimal.valueOf(1)));

            testPoint.charge(totalPrice.subtract(BigDecimal.ONE)); // 포인트 부족

            // act & assert
            assertThrows(IllegalStateException.class, () -> orderFacade.placeOrder(command));
        }

        @DisplayName("쿠폰을 적용하면, 할인된 금액만큼 포인트가 차감되고 쿠폰은 사용 처리된다")
        @Test
        void placeOrder_withCoupon_shouldApplyDiscountAndMarkCouponAsUsed() {
            // arrange
            testPoint.charge(DEFAULT_POINT_AMOUNT);
            pointRepository.save(testPoint);

            int orderQuantity = 2;

            CouponEntity coupon = couponRepository.save(CouponEntity.ofFixed("1000원 할인 쿠폰", testUser, 1000L));

            BigDecimal originalPrice = productA.getPrice().multiply(BigDecimal.valueOf(orderQuantity));
            BigDecimal discountAmount = coupon.getDiscountAmount(originalPrice);
            BigDecimal finalPrice = originalPrice.subtract(discountAmount);
            BigDecimal initialPoints = testPoint.getAmount();

            // act
            OrderCommand.Place command = OrderCommand.Place.create(
                    testUser.getId(),
                    List.of(new OrderCommand.OrderItemDetail(productA.getId(), orderQuantity)),
                    PaymentMethod.POINT,
                    coupon.getId()
            );
            orderFacade.placeOrder(command);

            // assert
            PointEntity userPointAfterOrder = pointRepository.findByUserId(testUser.getId()).orElseThrow();
            CouponEntity usedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();

            assertThat(userPointAfterOrder.getAmount()).isEqualTo(initialPoints.subtract(finalPrice));
            assertThat(usedCoupon.isUsed()).isTrue();
        }
    }

}

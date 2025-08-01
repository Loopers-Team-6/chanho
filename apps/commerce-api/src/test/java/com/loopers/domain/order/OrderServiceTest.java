package com.loopers.domain.order;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.order.FakeOrderRepository;
import com.loopers.infrastructure.point.FakePointRepository;
import com.loopers.infrastructure.product.FakeProductRepository;
import com.loopers.infrastructure.user.FakeUserRepository;
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

public class OrderServiceTest {

    /*
     * 주문 서비스 테스트
     *
     */

    private OrderService orderService;
    private UserEntity testUser;
    private PointEntity testPoint;
    private ProductEntity productA;
    private ProductEntity productB;

    private UserRepository userRepository;
    private ProductRepository productRepository;
    private OrderRepository orderRepository;
    private PointRepository pointRepository;

    @BeforeEach
    public void setUp() {
        userRepository = new FakeUserRepository();
        productRepository = new FakeProductRepository();
        orderRepository = new FakeOrderRepository();
        pointRepository = new FakePointRepository();
        orderService = new OrderService(orderRepository, productRepository, pointRepository);

        testUser = UserEntity.create(
                "testUser",
                "test@email.com",
                UserGender.M,
                LocalDate.now().minusYears(20));
        testPoint = new PointEntity(testUser);

        BrandEntity brand = new BrandEntity("나이키");
        productA = ProductEntity.create("ProductA", 10000, 10, brand);
        productB = ProductEntity.create("ProductB", 25000, 5, brand);
    }

    @DisplayName("주문을 생성할 때, ")
    @Nested
    class PlaceOrder {

        @DisplayName("사용자가 유효하지 않으면 예외가 발생한다")
        @Test
        void createOrderWithInvalidUser() {
            // arrange
            UserEntity invalidUser = null; // 유효하지 않은 사용자
            // act & assert
            assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(invalidUser, null));
        }

        @DisplayName("주문 항목이 유효하지 않으면 예외가 발생한다")
        @Test
        void createOrderWithInvalidCommand() {
            // arrange
            UserEntity validUser = userRepository.save(testUser);
            // act & assert
            assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(validUser, null));
        }

        @DisplayName("주문 항목이 유효하면 주문을 생성한다")
        @Test
        void createOrderWithValidCommand() {
            // arrange
            UserEntity validUser = userRepository.save(testUser);
            pointRepository.save(testPoint);
            testPoint.charge(10000000L);
            ProductEntity savedA = productRepository.save(productA);
            ProductEntity savedB = productRepository.save(productB);
            OrderCommand.Place command = new OrderCommand.Place(
                    validUser.getId(),
                    List.of(
                            new OrderCommand.OrderItemDetail(savedA.getId(), 2),
                            new OrderCommand.OrderItemDetail(savedB.getId(), 1)
                    )
            );

            // act
            orderService.placeOrder(validUser, command);

            // assert
            assertThat(orderRepository.findById(validUser.getId()))
                    .isPresent()
                    .get()
                    .extracting(OrderEntity::getUser)
                    .isEqualTo(validUser);
        }

        @DisplayName("주문 항목에 상품이 없으면 예외가 발생한다")
        @Test
        void createOrderWithEmptyItems() {
            // arrange
            UserEntity validUser = userRepository.save(testUser);
            OrderCommand.Place command = new OrderCommand.Place(
                    validUser.getId(),
                    Arrays.asList(null, null)
            );

            // act & assert
            assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(validUser, command));
        }

        @DisplayName("올바르게 주문이 생성되면, 재고가 차감된다")
        @Test
        void createOrderAndReduceStock() {
            // arrange
            UserEntity validUser = userRepository.save(testUser);
            pointRepository.save(testPoint);
            testPoint.charge(10000000L);
            ProductEntity savedA = productRepository.save(productA);
            ProductEntity savedB = productRepository.save(productB);
            int stockA = savedA.getStock();
            int stockB = savedB.getStock();

            int quantityA = 2;
            int quantityB = 1;

            OrderCommand.Place command = new OrderCommand.Place(
                    validUser.getId(),
                    List.of(
                            new OrderCommand.OrderItemDetail(savedA.getId(), quantityA),
                            new OrderCommand.OrderItemDetail(savedB.getId(), quantityB)
                    )
            );

            // act
            orderService.placeOrder(validUser, command);

            // assert
            ProductEntity updatedProductA = productRepository.findById(savedA.getId()).orElseThrow();
            ProductEntity updatedProductB = productRepository.findById(savedB.getId()).orElseThrow();

            assertThat(updatedProductA.getStock()).isEqualTo(stockA - quantityA);
            assertThat(updatedProductB.getStock()).isEqualTo(stockB - quantityB);
        }

        @DisplayName("주문이 완료되면, 주문 상태가 COMPLETED로 변경된다")
        @Test
        void completeOrderStatusAfterPlacingOrder() {
            // arrange
            UserEntity validUser = userRepository.save(testUser);
            pointRepository.save(testPoint);
            testPoint.charge(10000000L);
            ProductEntity savedA = productRepository.save(productA);
            ProductEntity savedB = productRepository.save(productB);
            OrderCommand.Place command = new OrderCommand.Place(
                    validUser.getId(),
                    List.of(
                            new OrderCommand.OrderItemDetail(savedA.getId(), 2),
                            new OrderCommand.OrderItemDetail(savedB.getId(), 1)
                    )
            );

            // act
            OrderInfo orderInfo = orderService.placeOrder(validUser, command);

            // assert
            assertThat(orderInfo.getOrderStatus()).isEqualTo(OrderEntity.OrderStatus.COMPLETED.name());
        }

        @DisplayName("주문이 완료되면 포인트가 차감된다")
        @Test
        void reducePointsAfterOrderCompletion() {
            // arrange
            UserEntity validUser = userRepository.save(testUser);
            ProductEntity savedA = productRepository.save(productA);
            ProductEntity savedB = productRepository.save(productB);
            pointRepository.save(testPoint);
            OrderCommand.Place command = new OrderCommand.Place(
                    validUser.getId(),
                    List.of(
                            new OrderCommand.OrderItemDetail(savedA.getId(), 2),
                            new OrderCommand.OrderItemDetail(savedB.getId(), 1)
                    )
            );
            BigDecimal totalPrice = savedA.getPrice().multiply(BigDecimal.valueOf(2))
                    .add(savedB.getPrice().multiply(BigDecimal.valueOf(1)));

            testPoint.charge(totalPrice.longValue());
            Long amountBeforeOrder = testPoint.getAmount();

            // act
            OrderInfo orderInfo = orderService.placeOrder(validUser, command);

            // assert
            assertThat(testPoint.getAmount()).isEqualTo(amountBeforeOrder - totalPrice.longValue());
        }

        @DisplayName("포인트가 모자라면 주문이 실패한다")
        @Test
        void failOrderWhenInsufficientPoints() {
            // arrange
            UserEntity validUser = userRepository.save(testUser);
            ProductEntity savedA = productRepository.save(productA);
            ProductEntity savedB = productRepository.save(productB);
            pointRepository.save(testPoint);
            OrderCommand.Place command = new OrderCommand.Place(
                    validUser.getId(),
                    List.of(
                            new OrderCommand.OrderItemDetail(savedA.getId(), 2),
                            new OrderCommand.OrderItemDetail(savedB.getId(), 1)
                    )
            );
            BigDecimal totalPrice = savedA.getPrice().multiply(BigDecimal.valueOf(2))
                    .add(savedB.getPrice().multiply(BigDecimal.valueOf(1)));

            testPoint.charge(totalPrice.longValue() - 1); // 포인트 부족

            // act & assert
            assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(validUser, command));
        }
    }

}

package com.loopers.application.order;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class OrderFacadeConcurrencyTest {

    /*
     * 주문 파사드 동시성 테스트
     */

    private static final BigDecimal DEFAULT_POINT_AMOUNT = BigDecimal.valueOf(1000000L);

    private UserEntity testUser;
    private ProductEntity productA;

    @Autowired
    private OrderFacade orderFacade;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PointRepository pointRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @BeforeEach
    public void setUp() {
        setUpTestData();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private void setUpTestData() {
        testUser = UserEntity.create(
                "test",
                "test@email.com",
                UserGender.MALE,
                LocalDate.now().minusYears(20));
    }

    @DisplayName("동시성 테스트")
    @Nested
    class PlaceOrderConcurrencyTest {

        @DisplayName("재고가 1개인 상품을 100명이 동시에 주문하면, 1개의 주문만 성공해야 한다.")
        @Test
        void onlyOneOrderShouldSucceed_whenStockIsOne() throws InterruptedException {
            // arrange
            final int threadCount = 100;
            UserEntity savedUser = userRepository.save(testUser);

            PointEntity point = pointRepository.save(new PointEntity(savedUser));
            point.charge(DEFAULT_POINT_AMOUNT);
            pointRepository.save(point);

            BrandEntity brand = brandRepository.save(BrandEntity.create("나이키"));
            productA = productRepository.save(ProductEntity.create("ProductA", 100, 1, brand));

            AtomicInteger successCount = new AtomicInteger();
            AtomicInteger failCount = new AtomicInteger();

            try (ExecutorService executorService = Executors.newFixedThreadPool(32)) {
                CountDownLatch latch = new CountDownLatch(threadCount);

                // act
                for (int i = 0; i < threadCount; i++) {
                    executorService.submit(() -> {
                        try {
                            OrderCommand.Place command = OrderCommand.Place.create(
                                    savedUser.getId(),
                                    List.of(new OrderCommand.OrderItemDetail(productA.getId(), 1)),
                                    PaymentMethod.POINT);
                            orderFacade.placeOrder(command);
                            successCount.getAndIncrement();
                        } catch (IllegalArgumentException e) {
                            if (e.getMessage().contains("재고가 부족합니다")) {
                                failCount.getAndIncrement();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            latch.countDown();
                        }
                    });
                }

                latch.await();
                executorService.shutdown();
            }

            // assert
            ProductEntity finalProduct = productRepository.findById(productA.getId()).orElseThrow();

            // 단 1개의 주문만 성공했는지 확인
            assertThat(successCount.get()).isEqualTo(1);

            // 나머지 주문은 모두 실패했는지 확인
            assertThat(failCount.get()).isEqualTo(threadCount - 1);

            // 최종 재고가 0인지 확인
            assertThat(finalProduct.getStock()).isZero();
        }

        @DisplayName("수량이 1개인 쿠폰을 동일한 사용자가 동시에 사용하면, 1개의 주문만 성공해야 한다.")
        @Test
        void onlyOneOrderShouldSucceed_whenUsingOneCouponConcurrently() throws InterruptedException {
            // arrange
            final int threadCount = 100;
            UserEntity userA = userRepository.save(UserEntity.create("userA", "a@test.com", UserGender.MALE, LocalDate.now().minusYears(20)));

            PointEntity pointA = pointRepository.save(new PointEntity(userA));
            pointA.charge(DEFAULT_POINT_AMOUNT);
            pointRepository.save(pointA);

            BrandEntity brand = brandRepository.save(BrandEntity.create("나이키"));
            ProductEntity product = productRepository.save(ProductEntity.create("Product A", 10000, 10, brand));
            CouponEntity coupon = couponRepository.save(CouponEntity.ofFixed("한정수량 쿠폰", userA, 1000L));

            AtomicInteger successCount = new AtomicInteger();
            AtomicInteger failCount = new AtomicInteger();
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            // act
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        orderFacade.placeOrder(new OrderCommand.Place(
                                userA.getId(),
                                List.of(new OrderCommand.OrderItemDetail(product.getId(), 1)),
                                PaymentMethod.POINT,
                                coupon.getId()
                        ));
                        successCount.getAndIncrement();
                    } catch (Exception e) {
                        failCount.getAndIncrement();
                        System.out.println("Order failed: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // assert
            // 단 1개의 주문만 성공했는지 확인
            assertThat(successCount.get()).isEqualTo(1);

            // 나머지 주문은 모두 실패했는지 확인
            assertThat(failCount.get()).isEqualTo(threadCount - 1);

            // 쿠폰이 사용된 상태인지 확인
            CouponEntity finalCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
            assertThat(finalCoupon.isUsed()).isTrue();

            // 주문이 생성되었는지 확인
            assertThat(orderRepository.count()).isEqualTo(1);
        }

        @DisplayName("포인트가 부족하면 주문이 실패하고, 사용했던 재고와 쿠폰은 모두 롤백되어야 한다.")
        @Test
        void stockAndCouponShouldBeRolledBack_whenPointIsInsufficient() {
            // arrange
            UserEntity user = userRepository.save(UserEntity.create("poorUser", "poor@test.com", UserGender.MALE, LocalDate.now().minusYears(20)));
            PointEntity point = new PointEntity(user);
            point.charge(BigDecimal.valueOf(100));
            pointRepository.save(point);

            BrandEntity brand = brandRepository.save(BrandEntity.create("샤넬"));
            int initialStock = 10;
            ProductEntity product = productRepository.save(ProductEntity.create("향수", 1000, initialStock, brand));

            CouponEntity coupon = couponRepository.save(CouponEntity.ofFixed("민생지원쿠폰", user, 100L));

            OrderCommand.Place command = new OrderCommand.Place(
                    user.getId(),
                    List.of(new OrderCommand.OrderItemDetail(product.getId(), 1)),
                    PaymentMethod.POINT,
                    coupon.getId()
            );

            // act & assert
            assertThrows(IllegalStateException.class, () -> {
                orderFacade.placeOrder(command);
            });

            // 상품 재고가 원상 복구되었는지 확인 (롤백)
            ProductEntity productAfterOrder = productRepository.findById(product.getId()).orElseThrow();
            assertThat(productAfterOrder.getStock()).isEqualTo(initialStock);

            // 쿠폰이 '사용 안 됨' 상태로 롤백되었는지 확인
            CouponEntity couponAfterOrder = couponRepository.findById(coupon.getId()).orElseThrow();
            assertThat(couponAfterOrder.isUsed()).isFalse();

            // 주문 자체가 생성되지 않았는지 확인
            assertThat(orderRepository.count()).isZero();
        }

        @DisplayName("동일한 유저가 서로 다른 주문을 동시에 수행해도, 포인트가 정상적으로 차감되어야 한다.")
        @Test
        void pointShouldBeDeductedCorrectly_whenSameUserOrdersConcurrently() throws InterruptedException {
            final int threadCount = 5;

            // arrange
            UserEntity user = userRepository.save(UserEntity.create("user", "test@test.com", UserGender.FEMALE, LocalDate.now().minusYears(25)));
            BigDecimal initialPoints = BigDecimal.valueOf(10000000);
            PointEntity point = pointRepository.save(new PointEntity(user));
            point.charge(initialPoints);
            pointRepository.save(point);

            BrandEntity brand = brandRepository.save(BrandEntity.create("madeInChina"));
            List<ProductEntity> products = new ArrayList<>();
            List<OrderCommand.Place> commands = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                products.add(productRepository.save(ProductEntity.create("상품" + (i + 1), 1000, 100, brand)));
                commands.add(OrderCommand.Place.create(user.getId(), List.of(new OrderCommand.OrderItemDetail(products.get(i).getId(), 1)), PaymentMethod.POINT));
            }

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger();

            // act
            for (int i = 0; i < threadCount; i++) {
                int finalI = i;
                executorService.submit(() -> {
                    try {
                        orderFacade.placeOrder(commands.get(finalI));
                        successCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // assert
            // 주문 모두 성공했는지 확인
            assertThat(successCount.get()).isEqualTo(threadCount);

            // 포인트가 정확하게 차감되었는지 확인
            PointEntity finalPoint = pointRepository.findById(point.getId()).orElseThrow();
            BigDecimal expectedPoints = initialPoints
                    .subtract(products.stream().map(ProductEntity::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
            assertThat(finalPoint.getAmount()).isEqualByComparingTo(expectedPoints);

            // 성공 횟수만큼 주문이 생성되었는지 확인
            assertThat(orderRepository.countByUser(user)).isEqualTo(commands.size());
        }
    }


}

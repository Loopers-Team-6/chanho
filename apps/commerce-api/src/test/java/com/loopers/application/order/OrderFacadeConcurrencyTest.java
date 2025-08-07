package com.loopers.application.order;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderRepository;
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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

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
                UserGender.M,
                LocalDate.now().minusYears(20));
    }

    @DisplayName("동시에 100개의 주문이 들어올 때, ")
    @Nested
    class PlaceOrderConcurrencyTest {

        final int threadCount = 100;

        @DisplayName("재고가 1개일 경우, 단 1개의 주문만 성공해야 한다.")
        @Test
        void onlyOneOrderShouldSucceed_whenStockIsOne() throws InterruptedException {
            // arrange
            // 사용자 생성
            UserEntity savedUser = userRepository.save(testUser);
            // 포인트 생성
            PointEntity point = pointRepository.save(new PointEntity(savedUser));
            point.charge(DEFAULT_POINT_AMOUNT);
            pointRepository.save(point);
            // 상품 A는 재고가 1개
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
                            OrderCommand.Place command = OrderCommand.Place.withoutCoupon(
                                    savedUser.getId(),
                                    List.of(new OrderCommand.OrderItemDetail(productA.getId(), 1))
                            );
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

            assertThat(successCount.get()).isEqualTo(1); // 단 1개의 주문만 성공했는지 확인
            assertThat(failCount.get()).isEqualTo(threadCount - 1); // 나머지 주문은 모두 실패했는지 확인
            assertThat(finalProduct.getStock()).isZero(); // 최종 재고가 0인지 확인
        }
    }
}

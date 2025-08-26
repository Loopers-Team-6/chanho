package com.loopers.interfaces.api.order;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderV1ApiE2ETest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    private ProductEntity product1;
    private UserEntity user;
    private PointEntity point;

    @BeforeEach
    void setUp() {
        BrandEntity brand = brandRepository.save(BrandEntity.create("Test Brand"));
        product1 = productRepository.save(ProductEntity.create("Test Product 1", 10000, 10, brand));
        user = userRepository.save(UserEntity.create("testuser", "test@test.com", UserGender.MALE, LocalDate.now().minusYears(20)));
        PointEntity point = PointEntity.create(user);
        point.charge(BigDecimal.valueOf(999999L));
        point = pointRepository.save(point);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/orders")
    @Nested
    class CreateOrder {

        /*
         * 주문 API 테스트
         * - [x] 주문 생성 시, 사용자 정보(헤더), 주문 항목, 결제수단, 쿠폰(선택)이 포함되어야 하며, 주문 정보를 반환한다.
         */

        private static final String ENDPOINT = "/api/v1/orders";

        @DisplayName("주문 생성 시, 사용자 정보(헤더), 주문 항목, 결제수단, 쿠폰(선택)이 포함되어야 한다")
        @Test
        void createOrderWithUserAndItems() {
            //arrange
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-USER-ID", user.getId().toString());
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            String orderRequestJson = """
                    {
                        "items": [
                            {
                                "productId": %d,
                                "quantity": 2
                            }
                        ],
                        "paymentMethod": "POINT",
                        "couponId": null
                    }
                    """.formatted(product1.getId());

            // act
            ParameterizedTypeReference<ApiResponse<OrderV1Dto.OrderResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<OrderV1Dto.OrderResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(orderRequestJson, httpHeaders), responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data()).isNotNull(),
                    () -> assertThat(response.getBody().data().items()).hasSize(1),
                    () -> assertThat(response.getBody().data().items().get(0).productId()).isEqualTo(product1.getId()),
                    () -> assertThat(response.getBody().data().items().get(0).quantity()).isEqualTo(2),
                    () -> assertThat(response.getBody().data().paymentMethod()).isEqualTo(PaymentMethod.POINT),
                    () -> assertThat(response.getBody().data().totalPrice()).isEqualTo(product1.getPrice().multiply(BigDecimal.valueOf(2)).toPlainString())
            );
        }
    }
}

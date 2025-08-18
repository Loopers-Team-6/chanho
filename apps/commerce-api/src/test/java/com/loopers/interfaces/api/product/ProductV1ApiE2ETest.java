package com.loopers.interfaces.api.product;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductV1ApiE2ETest {

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

    private ProductEntity product1;
    private UserEntity user1;

    @BeforeEach
    void setUp() {
        BrandEntity brand = brandRepository.save(BrandEntity.create("Test Brand"));
        product1 = productRepository.save(ProductEntity.create("Test Product 1", 10000, 10, brand));
        user1 = userRepository.save(UserEntity.create("testuser", "test@test.com", UserGender.M, LocalDate.now().minusYears(20)));
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("GET /api/v1/products/{id}")
    @Nested
    class GetProduct {

        private final Function<Long, String> ENDPOINT = productId -> "/api/v1/products/" + productId;

        @DisplayName("상품 상세 조회에 성공하면 상품 정보를 반환한다.")
        @Test
        void returnsProductInfo_whenProductExists() {
            // arrange
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-USER-ID", user1.getId().toString());

            // act
            ParameterizedTypeReference<ApiResponse<ProductV1Dto.ProductInfo>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<ProductV1Dto.ProductInfo>> response =
                    testRestTemplate.exchange(ENDPOINT.apply(product1.getId()), HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().id()).isEqualTo(product1.getId()),
                    () -> assertThat(response.getBody().data().name()).isEqualTo(product1.getName())
            );
        }
    }

    @DisplayName("GET /api/v1/products")
    @Nested
    class GetProducts {

        private static final String ENDPOINT = "/api/v1/products";

        @DisplayName("상품 목록 조회에 성공하면 상품 목록을 반환한다.")
        @Test
        void returnsProductList() {
            // arrange
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-USER-ID", user1.getId().toString());

            // act
            ParameterizedTypeReference<ApiResponse<PageResponse<ProductV1Dto.ProductInfo>>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<PageResponse<ProductV1Dto.ProductInfo>>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().getTotalElements()).isEqualTo(1)
            );
        }
    }
}

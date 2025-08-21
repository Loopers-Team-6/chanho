package com.loopers.application.product;

import com.loopers.application.ProductFacade;
import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.product.ProductV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class ProductFacadeCacheTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    // 메서드 호출을 추적해서 캐시 동작 확인
    @MockitoSpyBean
    private ProductService productService;

    private UserEntity testUser;
    private ProductEntity testProduct;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(UserEntity.create("testuser", "test@test.com", UserGender.MALE, LocalDate.now().minusYears(20)));
        BrandEntity brand = brandRepository.save(BrandEntity.create("Test Brand"));
        testProduct = productRepository.save(ProductEntity.create("Test Product", 10000, 10, brand));
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        Objects.requireNonNull(cacheManager.getCache("product")).clear();
        Objects.requireNonNull(cacheManager.getCache("products")).clear();
    }

    @DisplayName("getProduct 호출 시, 동일한 요청은 캐시를 통해 처리된다")
    @Test
    void getProduct_shouldBeCached() {
        // arrange
        Long productId = testProduct.getId();
        Long userId = testUser.getId();

        // act
        // 1. 첫 번째 호출 -> 캐시에 데이터 저장
        ProductV1Dto.ProductInfo firstResult = productFacade.getProduct(productId, userId);
        // 2. 두 번째 호출 -> 캐시에서 데이터 로드
        ProductV1Dto.ProductInfo cachedResult = productFacade.getProduct(productId, userId);

        // assert
        verify(productService, times(1)).findById(productId); // 실제 호출 횟수 1회
        assertThat(cachedResult).isNotNull();
        assertThat(firstResult).isEqualTo(cachedResult);
    }

    @DisplayName("getProducts 호출 시, 동일한 페이지 요청은 캐시를 통해 처리된다")
    @Test
    void getProducts_shouldBeCached() {
        // arrange
        Long userId = testUser.getId();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());

        // act
        // 1. 첫 번째 호출
        PageResponse<ProductV1Dto.ProductInfo> firstResult = productFacade.getProducts(pageable, userId);
        // 2. 두 번째 호출
        PageResponse<ProductV1Dto.ProductInfo> cachedResult = productFacade.getProducts(pageable, userId);

        // assert
        verify(productService, times(1)).findAll(pageable);
        assertThat(cachedResult).isNotNull();
        assertThat(firstResult).isEqualTo(cachedResult);
    }
}

package com.loopers.domain.product;

import com.loopers.domain.brand.BrandEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProductEntityTest {

    /*
     * 상품 단위 테스트
     * - [o]  상품은 이름, 가격, 재고, 브랜드를 가지고 있어야 한다
     * - [o]  상품 이름은 반드시 존재해야 하며, 50자 이내여야 한다
     * - [o]  상품 가격은 0 이상이어야 한다
     * - [o]  상품 재고는 0 이상이어야 한다
     * - [o]  상품 브랜드는 반드시 존재해야 한다
     * - [o]  상품은 재고를 가지고 있고, 주문 시 차감할 수 있어야 한다
     * - [o]  재고는 감소만 가능하며 음수 방지는 도메인 레벨에서 처리된다
     */

    @DisplayName("상품을 생성할 때, ")
    @Nested
    class CreateProduct {

        public static Stream<Arguments> invalidProductDataProvider() {
            return Stream.of(
                    Arguments.of(null, 100, 10, BrandEntity.create("나이키")), // 상품 이름이 null
                    Arguments.of("", 100, 10, BrandEntity.create("나이키")), // 상품 이름이 빈 문자열
                    Arguments.of("Test Product", -1, 10, BrandEntity.create("나이키")), // 가격이 음수
                    Arguments.of("Test Product", 100, -1, BrandEntity.create("나이키")), // 재고가 음수
                    Arguments.of("Test Product", 100, 10, null) // 브랜드가 null
            );
        }

        @DisplayName("상품 이름, 가격, 재고, 브랜드가 유효하면, 상품 생성에 성공한다")
        @Test
        void createProductEntityWithValidData() {
            // arrange
            String productName = "Test Product";
            int price = 100;
            int stock = 10;
            BrandEntity brand = BrandEntity.create("나이키");

            // act
            ProductEntity product = ProductEntity.create(productName, price, stock, brand);

            // assert
            assertThat(product.getName()).isEqualTo(productName);
            assertThat(product.getPrice()).isEqualTo(BigDecimal.valueOf(price));
            assertThat(product.getStock()).isEqualTo(stock);
            assertThat(product.getBrand()).isEqualTo(brand);
        }

        @DisplayName("이름, 가격, 재고, 브랜드 중 하나라도 유효하지 않으면, 상품 생성에 실패한다")
        @MethodSource("invalidProductDataProvider")
        @ParameterizedTest
        void throwException_whenProductDataIsInvalid(String productName, int price, int stock, BrandEntity brand) {
            // act & assert
            assertThrows(IllegalArgumentException.class, () -> ProductEntity.create(productName, price, stock, brand));
        }
    }

    @DisplayName("상품 재고를 차감할 때, ")
    @Nested
    class ReduceStock {

        @DisplayName("재고가 충분할 경우, 재고를 차감한다")
        @Test
        void reduceStock_whenSufficientStock() {
            String productName = "Test Product";
            int price = 100;
            int initialStock = 10;
            BrandEntity brand = BrandEntity.create("나이키");

            // arrange
            ProductEntity product = ProductEntity.create(
                    productName,
                    price,
                    initialStock,
                    brand);

            int quantityToReduce = 5;

            // act
            product.decreaseStock(quantityToReduce);

            // assert
            assertThat(product.getStock()).isEqualTo(initialStock - quantityToReduce);
        }

        @DisplayName("재고가 부족할 경우, 예외를 발생시킨다")
        @Test
        void throwException_whenInsufficientStock() {
            String productName = "Test Product";
            int price = 100;
            int initialStock = 5;
            BrandEntity brand = BrandEntity.create("나이키");

            // arrange
            ProductEntity product = ProductEntity.create(
                    productName,
                    price,
                    initialStock,
                    brand);

            int quantityToReduce = 10;

            // act & assert
            assertThrows(IllegalArgumentException.class, () -> product.decreaseStock(quantityToReduce));
        }
    }
}

package com.loopers.domain.order;

import com.loopers.application.order.OrderItemInfo;
import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderEntityTest {

    /*
     * 주문 엔티티 테스트
     * - [o]  주문 생성 시, 사용자 정보가 유효해야 한다
     * - [o]  주문 항목 추가 시, 상품 ID, 이름, 가격, 수량이 유효해야 한다
     * - [o]  주문은 총 가격을 계산할 수 있다
     */

    private UserEntity testUser;
    private ProductEntity productA;
    private ProductEntity productB;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.create(
                "testUser",
                "test@email.com",
                UserGender.M,
                LocalDate.now().minusYears(20));

        BrandEntity brand = BrandEntity.create("나이키");
        productA = ProductEntity.create("ProductA", 10000, 10, brand);
        productB = ProductEntity.create("ProductB", 25000, 5, brand);
    }

    @DisplayName("주문을 생성할 때, ")
    @Nested
    class CreateOrder {

        @DisplayName("사용자가 유효하면, 주문 생성에 성공한다")
        @Test
        void createOrderEntityWithValidUserAndProducts() {
            // act
            OrderEntity order = OrderEntity.create(testUser);

            // assert
            assertThat(order.getUser()).isEqualTo(testUser);
        }

        @DisplayName("사용자 정보가 유효하지 않으면, 예외가 발생한다")
        @Test
        void createOrderEntityWithInvalidUser() {
            // arrange
            // act & assert
            assertThrows(IllegalArgumentException.class, () -> OrderEntity.create(null));
        }
    }

    @DisplayName("주문에 상품을 추가할 때, ")
    @Nested
    class AddOrderItem {

        @DisplayName("주문 항목이 유효하지 않으면, 예외가 발생한다")
        @Test
        void throwExceptions_whenInvalidItemIsAdded() {
            // arrange
            Long invalidProductId = null; // 유효하지 않은 상품 ID
            String invalidProductName = ""; // 유효하지 않은 상품 이름
            BigDecimal invalidPrice = new BigDecimal("-1"); // 유효하지 않은 가격
            int invalidQuantity = 0; // 유효하지 않은 수량

            Long productId = productA.getId();
            String productName = productA.getName();
            BigDecimal price = productA.getPrice();
            int quantity = 2;

            // act & assert
            assertThrows(IllegalArgumentException.class,
                    () -> new OrderItemInfo(invalidProductId, productName, price, quantity)); // 상품 ID 없음
            assertThrows(IllegalArgumentException.class,
                    () -> new OrderItemInfo(productId, invalidProductName, price, quantity)); // 상품 이름 없음
            assertThrows(IllegalArgumentException.class,
                    () -> new OrderItemInfo(productId, productName, invalidPrice, quantity)); // 가격이 0 이하
            assertThrows(IllegalArgumentException.class,
                    () -> new OrderItemInfo(productId, productName, price, invalidQuantity)); // 수량이 0 이하
        }

        @DisplayName("유효한 주문 항목을 추가하면, 상품 총 가격이 계산된다")
        @Test
        void addValidOrderItemAndCalculateTotalPrice() {
            // arrange
            OrderEntity order = OrderEntity.create(testUser);

            Long productIdA = productA.getId();
            String productNameA = productA.getName();
            BigDecimal priceA = productA.getPrice();
            int quantityA = 2;

            Long productIdB = productB.getId();
            String productNameB = productB.getName();
            BigDecimal priceB = productB.getPrice();
            int quantityB = 1;

            BigDecimal totalPrice = priceA.multiply(BigDecimal.valueOf(quantityA))
                    .add(priceB.multiply(BigDecimal.valueOf(quantityB)));

            // act
            order.addOrderItem(new OrderItemInfo(productIdA, productNameA, priceA, quantityA));
            order.addOrderItem(new OrderItemInfo(productIdB, productNameB, priceB, quantityB));

            // assert
            assertThat(order.getItems()).hasSize(2);
            assertThat(order.getOriginalPrice()).isEqualTo(totalPrice);
        }
    }
}

package com.loopers.domain.payment;

import com.loopers.application.order.OrderItemInfo;
import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class PaymentEntityTest {

    /*
     * 결제 도메인 테스트
     * - [x] 주문 정보를 받아 CREATED 상태로 결제 요청을 생성한다.
     * - [x] 결제 요청은 주문의 최종 가격을 금액으로 설정한다.
     * - [x] 결제 요청을 완료하면 PENDING 상태로 변경한다.
     * - [x] 결제 요청을 취소하면 CANCELED 상태로 변경한다
     * - [x] 결제 요청이 실패하면 FAILED 상태로 변경한다.
     * - [x] 결제 요청의 상태는 멱등적으로 변경된다.
     * - [x] 한 번 변경된 상태는 이전으로 돌아갈 수 없다.
     */

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = UserEntity.create(
                "testUser",
                "test@test.com",
                UserGender.MALE,
                LocalDate.now().minusYears(20)
        );
    }

    @DisplayName("결제 요청 생성")
    @Nested
    class CreatePaymentRequest {

        @DisplayName("주문 정보를 받아 PENDING 상태로 결제 요청을 생성한다")
        @Test
        void createPendingPaymentRequest() {
            // arrange
            OrderEntity order = OrderEntity.create(user);

            // act
            PaymentEntity payment = PointPaymentEntity.create(order);

            // assert
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @DisplayName("결제 요청은 주문의 최종 가격을 금액으로 설정한다")
        @Test
        void createPaymentWithOrderFinalPrice() {
            // arrange
            OrderEntity order = OrderEntity.create(user);
            order.addOrderItem(new OrderItemInfo(
                    1L,
                    "Test Product",
                    BigDecimal.valueOf(10000),
                    2
            ));

            // act
            PaymentEntity payment = PointPaymentEntity.create(order);

            // assert
            assertThat(payment.getAmount()).isEqualTo(order.getFinalPrice());
        }
    }

    @DisplayName("결제 요청 상태 변경 시,")
    @Nested
    class ChangePaymentStatus {

        @DisplayName("결제 요청을 완료하면 COMPLETED 상태로 변경한다")
        @Test
        void completePaymentRequest() {
            // arrange
            OrderEntity order = OrderEntity.create(user);
            PaymentEntity payment = PointPaymentEntity.create(order);

            // act
            payment.markAsSuccess();

            // assert
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }

        @DisplayName("결제 요청을 취소하면 CANCELED 상태로 변경한다")
        @Test
        void cancelPaymentRequest() {
            // arrange
            OrderEntity order = OrderEntity.create(user);
            PaymentEntity payment = PointPaymentEntity.create(order);

            // act
            payment.markAsCanceled();

            // assert
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
        }

        @DisplayName("결제 요청이 실패하면 FAILED 상태로 변경한다")
        @Test
        void failPaymentRequest() {
            // arrange
            OrderEntity order = OrderEntity.create(user);
            PaymentEntity payment = PointPaymentEntity.create(order);

            // act
            payment.markAsFailed();

            // assert
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @DisplayName("결제 요청의 상태는 멱등적으로 변경된다")
        @Test
        void idempotentPaymentStatusChange() {
            // arrange
            OrderEntity order = OrderEntity.create(user);
            PaymentEntity payment1 = PointPaymentEntity.create(order);
            PaymentEntity payment2 = PointPaymentEntity.create(order);
            PaymentEntity payment3 = PointPaymentEntity.create(order);

            // act & assert
            payment1.markAsSuccess();
            assertThat(payment1.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            payment1.markAsSuccess();
            assertThat(payment1.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            payment1.markAsSuccess();
            assertThat(payment1.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            payment2.markAsCanceled();
            assertThat(payment2.getStatus()).isEqualTo(PaymentStatus.CANCELED);
            payment2.markAsCanceled();
            assertThat(payment2.getStatus()).isEqualTo(PaymentStatus.CANCELED);
            payment2.markAsCanceled();
            assertThat(payment2.getStatus()).isEqualTo(PaymentStatus.CANCELED);
            payment3.markAsFailed();
            assertThat(payment3.getStatus()).isEqualTo(PaymentStatus.FAILED);
            payment3.markAsFailed();
            assertThat(payment3.getStatus()).isEqualTo(PaymentStatus.FAILED);
            payment3.markAsFailed();
            assertThat(payment3.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @DisplayName("이미 변경된 상태는 다시 변경할 수 없다")
        @Test
        void invalidPaymentStatusTransition() {
            // arrange
            OrderEntity order = OrderEntity.create(user);
            PaymentEntity payment1 = PointPaymentEntity.create(order);
            PaymentEntity payment2 = PointPaymentEntity.create(order);
            PaymentEntity payment3 = PointPaymentEntity.create(order);

            // act & assert
            payment1.markAsSuccess();
            assertThat(payment1.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            payment2.markAsCanceled();
            assertThat(payment2.getStatus()).isEqualTo(PaymentStatus.CANCELED);
            payment3.markAsFailed();
            assertThat(payment3.getStatus()).isEqualTo(PaymentStatus.FAILED);

            // 동일한 상태로 변경시 멱등적으로 동작한다
            payment1.markAsSuccess();
            payment2.markAsCanceled();
            payment3.markAsFailed();

            // 이미 변경된 상태는 다시 변경할 수 없다
            assertThrows(IllegalStateException.class, payment1::markAsCanceled);
            assertThrows(IllegalStateException.class, payment1::markAsFailed);
            assertThrows(IllegalStateException.class, payment2::markAsSuccess);
            assertThrows(IllegalStateException.class, payment2::markAsFailed);
            assertThrows(IllegalStateException.class, payment3::markAsSuccess);
            assertThrows(IllegalStateException.class, payment3::markAsCanceled);
        }

    }
}

package com.loopers.domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

public class PaymentServiceTest {

    /*
     * 결제 서비스 테스트
     * - [ ] 결제 요청을 생성할 때 주문 정보를 받아 PENDING 상태로 결제 요청을 생성한다.
     * - [ ] 결제 요청은 주문의 최종 가격을 금액으로 설정한다.
     * - [ ] 결제 요청을 완료하면 COMPLETED 상태로 변경한다.
     * - [ ] 결제 요청을 취소하면 CANCELED 상태로 변경한다
     * - [ ] 결제 요청이 실패하면 FAILED 상태로 변경한다.
     * - [ ] 결제 요청의 상태는 멱등적으로 변경된다.
     * - [ ] 이미 변경된 상태는 다시 변경할 수 없다.
     * - [ ] 결제 요청의 상태 변경 시 이벤트를 발행한다.
     * - [ ] 결제수단 별로 결제 요청을 처리한다 (예: 포인트, 카드 등).
     */

    @DisplayName("결제 요청 생성 시, ")
    @Nested
    class CreatePaymentRequest {

        @DisplayName("주문 정보를 받아 PENDING 상태로 결제 요청을 생성한다")
        void createPendingPaymentRequest() {
            
        }
    }
}

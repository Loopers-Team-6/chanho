package com.loopers.domain.payment;

public enum PaymentStatus {
    PENDING,   // 결제 요청이 생성된 상태
    COMPLETED, // 결제가 완료된 상태
    CANCELED,  // 결제가 취소된 상태
    FAILED     // 결제에 실패한 상태
}

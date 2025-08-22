package com.loopers.interfaces.api.payment;

import java.math.BigDecimal;

public class PaymentV1Dto {

    /**
     * 트랜잭션 정보
     *
     * @param transactionKey 트랜잭션 KEY
     * @param orderId        주문 ID
     * @param cardType       카드 종류
     * @param cardNo         카드 번호
     * @param amount         금액
     * @param status         처리 상태
     * @param reason         처리 사유
     */
    public record TransactionInfo(
            String transactionKey,
            Long orderId,
            CardType cardType,
            String cardNo,
            BigDecimal amount,
            TransactionStatus status,
            String reason
    ) {
        public TransactionInfo {
            if (transactionKey == null || transactionKey.isBlank()) {
                throw new IllegalArgumentException("트랜잭션 KEY는 필수입니다");
            }
            if (orderId == null || orderId <= 0) {
                throw new IllegalArgumentException("주문 ID는 올바른 값이어야 합니다");
            }
            if (cardType == null) {
                throw new IllegalArgumentException("카드 종류는 필수입니다");
            }
            if (cardNo == null || cardNo.isBlank()) {
                throw new IllegalArgumentException("카드 번호는 필수입니다");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("금액은 1 이상이어야 합니다");
            }
            if (status == null) {
                throw new IllegalArgumentException("처리 상태는 필수입니다");
            }
        }
    }
}

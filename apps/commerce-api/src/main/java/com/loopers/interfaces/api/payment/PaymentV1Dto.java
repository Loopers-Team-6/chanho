package com.loopers.interfaces.api.payment;

import java.math.BigDecimal;
import java.util.List;

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
    public record CallbackTransactionInfo(
            String transactionKey,
            Long orderId,
            CardType cardType,
            String cardNo,
            BigDecimal amount,
            TransactionStatus status,
            String reason
    ) {
        public CallbackTransactionInfo {
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

    public record Request(
            Long orderId,
            CardType cardType,
            String cardNo,
            String amount,
            String callbackUrl
    ) {
        public static Request create(
                Long orderId,
                CardType cardType,
                String cardNo,
                String amount,
                String callbackUrl
        ) {
            return new Request(orderId, cardType, cardNo, amount, callbackUrl);
        }

        public Request {
            if (orderId == null || orderId <= 0) {
                throw new IllegalArgumentException("주문 ID는 올바른 값이어야 합니다");
            }
            if (cardType == null) {
                throw new IllegalArgumentException("카드 종류는 필수입니다");
            }
            if (cardNo == null || cardNo.isBlank()) {
                throw new IllegalArgumentException("카드 번호는 필수입니다");
            }
            if (amount == null || amount.isBlank()) {
                throw new IllegalArgumentException("금액은 필수입니다");
            }
            if (callbackUrl == null || callbackUrl.isBlank()) {
                throw new IllegalArgumentException("콜백 URL은 필수입니다");
            }
        }
    }

    public record TransactionResponse(
            MetaData meta,
            TransactionInfo data
    ) {
        public TransactionResponse {
            if (meta == null) {
                throw new IllegalArgumentException("메타 정보는 필수입니다");
            }
        }

        public boolean isSuccess() {
            return meta.isSuccess();
        }
    }

    public record TransactionDetailResponse(
            MetaData meta,
            Data data
    ) {
        public record Data(
                String transactionKey,
                String orderId,
                CardType cardType,
                String cardNo,
                String amount,
                TransactionStatus status,
                String reason
        ) {
        }
    }

    public record OrderResponse(
            MetaData meta,
            Data data
    ) {

        public record Data(
                String orderId,
                List<TransactionInfo> transactions
        ) {
        }

        public OrderResponse {
            if (meta == null) {
                throw new IllegalArgumentException("메타 정보는 필수입니다");
            }
        }

    }

    public record MetaData(
            Result result,
            String errorCode,
            String message
    ) {
        public enum Result {
            SUCCESS,
            FAIL
        }

        public MetaData {
            if (result == null) {
                throw new IllegalArgumentException("result는 필수입니다");
            }
        }

        public boolean isSuccess() {
            return result == Result.SUCCESS;
        }

        public boolean isFail() {
            return result == Result.FAIL;
        }
    }

    public record TransactionInfo(
            String transactionKey,
            TransactionStatus status,
            String reason
    ) {
    }
}

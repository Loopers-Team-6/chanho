package com.loopers.infrastructure.payment.dto;

import com.loopers.domain.payment.PaymentStatus;
import com.loopers.infrastructure.payment.CardType;

import java.util.List;
import java.util.Map;

public class PgApiDto {

    public enum TransactionStatus {
        PENDING,
        SUCCESS,
        FAILED;

        private static final Map<TransactionStatus, PaymentStatus> STATUS_MAP = Map.of(
                PENDING, PaymentStatus.PENDING,
                SUCCESS, PaymentStatus.SUCCESS,
                FAILED, PaymentStatus.FAILED
        );

        public PaymentStatus toPaymentStatus() {
            return STATUS_MAP.get(this);
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

        public boolean isFail() {
            return meta.isFail();
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

        public boolean isFail() {
            return meta.isFail();
        }

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

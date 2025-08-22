package com.loopers.application.payment;

import com.loopers.interfaces.api.payment.TransactionStatus;

public class PaymentCommand {

    public record Update(
            TransactionStatus status,
            String transactionKey
    ) {
        public static Update create(TransactionStatus status, String transactionKey) {
            if (status == null || transactionKey == null || transactionKey.isEmpty()) {
                throw new IllegalArgumentException("상태와 트랜잭션 키는 필수입니다.");
            }
            return new Update(status, transactionKey);
        }
    }
}

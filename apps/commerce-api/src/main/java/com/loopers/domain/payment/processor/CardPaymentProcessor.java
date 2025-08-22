package com.loopers.domain.payment.processor;

import com.loopers.domain.payment.CardPaymentEntity;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.infrastructure.payment.PgClient;
import com.loopers.interfaces.api.payment.CardType;
import com.loopers.interfaces.api.payment.PaymentV1Dto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardPaymentProcessor implements PaymentProcessor {

    private final PgClient pgClient;

    @Value("${pg.simulator.callbackUrl}")
    private String callbackUrl;

    @Value("${pg.user-id}")
    private long pgUserId;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    public PaymentEntity createPayment(Long orderId, BigDecimal amount) {
        return CardPaymentEntity.create(orderId, amount, null);
    }

    @Override
    public void processPayment(PaymentEntity payment) {
        PaymentV1Dto.Request request = PaymentV1Dto.Request.create(
                payment.getOrderId(),
                CardType.KB,
                "1234-5678-9012-3456",
                payment.getAmount().toPlainString(),
                callbackUrl
        );

        try {
            PaymentV1Dto.TransactionResponse response = pgClient.createPaymentRequest(pgUserId, request);
            log.info("Payment request response: {}", response);

            if (response.isSuccess()) {
                CardPaymentEntity cardPayment = (CardPaymentEntity) payment;
                cardPayment.setTransactionKey(response.data().transactionKey());
                return;
            } else {
                log.info("Payment request failed: {}", response.meta());
                payment.fail();
            }
        } catch (FeignException e) {
            log.error("Payment request failed with FeignException:", e);
            payment.fail();
        }
    }
}

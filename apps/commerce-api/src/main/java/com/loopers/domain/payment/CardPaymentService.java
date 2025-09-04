package com.loopers.domain.payment;

import com.loopers.infrastructure.payment.CardType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardPaymentService {

    private final ApplicationEventPublisher eventPublisher;
    private final CardPaymentClient cardPaymentClient;
    private final PaymentRepository paymentRepository;

    @Value("${pg.simulator.callbackUrl}")
    private String callbackUrl;

    public void processCardPayment(Long paymentId, Long orderId, BigDecimal amount) {
        Optional<CardPaymentClient.FindPaymentsResponse> transactions = cardPaymentClient.findTransactionsByOrderId(orderId);
        transactions.ifPresentOrElse(
                response -> {
                    log.info("이미 결제 요청된 주문입니다. orderId: [{}]", orderId);
                    updatePayment(paymentId, orderId, amount, response.transactions().getLast());
                },
                requestNewTransaction(paymentId, orderId, amount)
        );
    }

    private Runnable requestNewTransaction(Long paymentId, Long orderId, BigDecimal amount) {
        return () -> {
            log.info("결제 요청 내역이 아직 없습니다. 신규 결제를 요청합니다. orderId: [{}]", orderId);
            var request = new CardPaymentClient.PaymentRequest(
                    orderId,
                    amount,
                    CardType.SAMSUNG,
                    "1234-5678-9012-3456",
                    callbackUrl
            );

            Optional<CardPaymentClient.TransactionInfo> transactionInfo = cardPaymentClient.requestPayment(request);
            transactionInfo.ifPresentOrElse(
                    response -> {
                        log.info("PG 결제 요청 성공: orderId: {}", orderId);
                        updatePayment(paymentId, orderId, amount, response);
                    },
                    () -> {
                        throw new IllegalStateException("PG 결제 요청 실패");
                    }
            );
        };
    }

    private void updatePayment(
            Long paymentId, Long orderId, BigDecimal amount, CardPaymentClient.TransactionInfo transactionInfo
    ) {
        String transactionKey = transactionInfo.transactionKey();
        PaymentStatus paymentStatus = transactionInfo.status();
        eventPublisher.publishEvent(new CardPaymentResponseEvent(
                paymentId, orderId, amount, transactionKey, paymentStatus));
    }

    @Transactional
    public void updatePaymentTransactionInfo(Long paymentId, String transactionKey, PaymentStatus status) {
        CardPaymentEntity payment = (CardPaymentEntity) paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("결제 내역을 찾을 수 없습니다. paymentId: " + paymentId));
        payment.updateTransactionInfo(transactionKey, status);
        paymentRepository.save(payment);
        eventPublisher.publishEvent(new PaymentProcessedEvent(payment.getOrderId(), payment.getStatus()));
    }
}

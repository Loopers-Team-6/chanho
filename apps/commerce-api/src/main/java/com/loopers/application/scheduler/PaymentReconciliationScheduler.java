package com.loopers.application.scheduler;

import com.loopers.application.payment.PaymentCommand;
import com.loopers.domain.payment.*;
import com.loopers.infrastructure.payment.PgClient;
import com.loopers.interfaces.api.payment.PaymentV1Dto;
import com.loopers.interfaces.api.payment.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentReconciliationScheduler {

    private final PaymentRepository paymentRepository;
    private final PgClient pgClient;
    private final PaymentService paymentService;

    @Value("${pg.user-id}")
    private long pgUserId;

    // 10초
    @Scheduled(fixedDelay = 10000)
    public void reconcilePendingPayments() {
        log.info("대기 중인 결제 상태 확인 스케줄러 시작");

        // 1. 생성된 지 5분이 지난 PENDING 상태의 결제건 조회
        ZonedDateTime threshold = ZonedDateTime.now().minusMinutes(5);
        List<PaymentEntity> pendingPayments = paymentRepository.findAllByStatusAndCreatedAtBefore(PaymentStatus.PENDING, threshold);

        if (pendingPayments.isEmpty()) {
            log.info("대기 중인 결제 건이 없습니다.");
            return;
        }

        for (PaymentEntity payment : pendingPayments) {
            try {
                // 2. PG사에 해당 결제건의 최종 상태 요청
                CardPaymentEntity cardPayment = (CardPaymentEntity) payment;
                String transactionKey = cardPayment.getTransactionKey();
                PaymentV1Dto.TransactionDetailResponse pgStatus = pgClient.getPaymentTransactionDetail(pgUserId, transactionKey);

                TransactionStatus transactionStatus = pgStatus.data().status();
                paymentService.processPaymentCallback(
                        new PaymentCommand.Update(transactionStatus, transactionKey)
                );
                log.info("결제 ID {}의 상태를 {}로 업데이트했습니다.", payment.getId(), transactionStatus);

            } catch (Exception e) {
                log.error("보류 중인 결제(ID: {}) 상태 확인 중 오류 발생", payment.getId(), e);
            }
        }
        log.info("보류 중인 결제 상태 확인 스케줄러 종료");
    }
}

package com.loopers.application.scheduler;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentReconciliationScheduler {

    private final PaymentService paymentService;

    // 10초
    @Scheduled(fixedDelay = 10000)
    public void retryPayments() {
        // 1. 생성된 지 5분이 지난 CREATED 상태의 미결제건 조회
        ZonedDateTime threshold = ZonedDateTime.now().minusMinutes(5);
        List<PaymentEntity> payments = paymentService.findPaymentsToRetry(threshold);
        if (payments.isEmpty()) {
            log.info("결제 요청 재시도 건이 없습니다.");
            return;
        }

        payments.forEach(paymentService::processAndSyncStatus);
    }

    @Scheduled(fixedDelay = 10000)
    public void reconcilePendingPayments() {
        // 1. 생성된 지 5분이 지난 PENDING 상태의 결제건 조회
        ZonedDateTime threshold = ZonedDateTime.now().minusMinutes(5);
        List<PaymentEntity> payments = paymentService.findPendingPayments(threshold);
        if (payments.isEmpty()) {
            log.info("대기 중인 결제 요청이 없습니다.");
            return;
        }

        payments.forEach(paymentService::processAndSyncStatus);
    }
}

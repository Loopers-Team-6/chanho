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
            return;
        }

        log.info("결제 요청 재시도 대상 {}건에 대해 재시도 시도", payments.size());
        payments.forEach(paymentService::processAndSyncStatus);
    }

    @Scheduled(fixedDelay = 10000)
    public void reconcilePendingPayments() {
        // 1. 생성된 지 5분이 지난 PENDING 상태의 결제건 조회
        ZonedDateTime threshold = ZonedDateTime.now().minusMinutes(5);
        List<PaymentEntity> payments = paymentService.findPendingPayments(threshold);
        if (payments.isEmpty()) {
            return;
        }

        log.info("대기 중인 결제 요청 {}건에 대해 상태 동기화 시도", payments.size());
        payments.forEach(paymentService::processAndSyncStatus);
    }
}

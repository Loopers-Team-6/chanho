package com.loopers.infrastructure.payment;

import com.loopers.interfaces.api.payment.PaymentV1Dto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "pg-client", url = "${pg.simulator.url}")
public interface PgClient {

    @PostMapping("/api/v1/payments")
    PaymentV1Dto.TransactionResponse createPaymentRequest(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody PaymentV1Dto.Request paymentRequest
    );

    @GetMapping("/api/v1/payments/{transactionKey}")
    PaymentV1Dto.TransactionDetailResponse findPaymentTransactionDetailByKey(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable("transactionKey") String transactionKey
    );

    @GetMapping("/api/v1/payments")
    PaymentV1Dto.OrderResponse findPaymentsByOrderId(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam("orderId") Long orderId
    );
}

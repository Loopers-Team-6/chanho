package com.loopers.infrastructure.payment;

import com.loopers.infrastructure.payment.dto.PgApiDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "pg-client", url = "${pg.simulator.url}")
public interface PgClient {

    @PostMapping("/api/v1/payments")
    PgApiDto.TransactionResponse createPaymentRequest(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody PgApiDto.Request paymentRequest
    );

    @GetMapping("/api/v1/payments/{transactionKey}")
    PgApiDto.TransactionDetailResponse findPaymentTransactionDetailByKey(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable("transactionKey") String transactionKey
    );

    @GetMapping("/api/v1/payments")
    PgApiDto.OrderResponse findTransactionsByOrderId(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam("orderId") Long orderId
    );
}

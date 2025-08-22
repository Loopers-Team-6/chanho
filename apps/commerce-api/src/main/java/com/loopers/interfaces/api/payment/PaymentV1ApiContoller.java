package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentV1ApiContoller implements PaymentV1ApiSpec {

    @PostMapping("/callback")
    @Override
    public ApiResponse<Void> paymentCallback(@RequestBody PaymentV1Dto.TransactionInfo transactionInfo) {
        System.out.println("Payment callback received: " + transactionInfo);
        return null;
    }
}

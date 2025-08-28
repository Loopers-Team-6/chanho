package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentV1ApiContoller implements PaymentV1ApiSpec {

    private final PaymentService paymentService;

    @PostMapping("/callback")
    @Override
    public void paymentCallback(@RequestBody PaymentV1Dto.CallbackTransactionInfo transactionInfo) {
        log.info("결제 콜백 수신: {}", transactionInfo);
        PaymentCommand.Update command = PaymentCommand.Update.create(transactionInfo.getPaymentStatus(), transactionInfo.transactionKey());
        paymentService.confirmPayment(command);
    }
}

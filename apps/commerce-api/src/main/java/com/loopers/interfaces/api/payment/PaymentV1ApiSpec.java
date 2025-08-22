package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Payment V1 API", description = "my-commerce 결제 API 입니다.")
public interface PaymentV1ApiSpec {

    @Operation(summary = "결제 콜백 API")
    ApiResponse<Void> paymentCallback(@RequestBody PaymentV1Dto.TransactionInfo transactionInfo);
}

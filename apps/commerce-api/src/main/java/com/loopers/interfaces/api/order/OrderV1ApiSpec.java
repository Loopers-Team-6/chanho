package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "Order V1 API", description = "my-commerce 주문 API 입니다.")
public interface OrderV1ApiSpec {

    @Operation(summary = "주문 생성")
    ApiResponse<OrderV1Dto.OrderResponse> createOrder(
            HttpServletRequest request,
            OrderV1Dto.OrderRequest orderRequest
    );

    @Operation(summary = "주문 정보 조회")
    ApiResponse<OrderV1Dto.OrderResponse> getOrder(
            HttpServletRequest request,
            Long orderId
    );
}

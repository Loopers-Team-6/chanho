package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCommand;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderV1ApiController implements OrderV1ApiSpec {

    private final OrderFacade orderFacade;

    @PostMapping
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> createOrder(HttpServletRequest request, @RequestBody OrderV1Dto.OrderRequest orderRequest) {
        if (request.getHeader("X-USER-ID") == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "User ID is missing in the request header.");
        }

        long userId = Long.parseLong(request.getHeader("X-USER-ID"));
        OrderInfo orderInfo = orderFacade.placeOrder(OrderCommand.Place.create(
                userId,
                orderRequest.items().stream()
                        .map(item -> new OrderCommand.OrderItemDetail(item.productId(), item.quantity()))
                        .toList(),
                orderRequest.paymentMethod(),
                orderRequest.couponId()
        ));

        return ApiResponse.success(OrderV1Dto.OrderResponse.from(orderInfo));
    }

    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> getOrder(HttpServletRequest request, @PathVariable Long orderId) {
        if (request.getHeader("X-USER-ID") == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "User ID is missing in the request header.");
        }

        long userId = Long.parseLong(request.getHeader("X-USER-ID"));
        OrderInfo orderInfo = orderFacade.getOrder(OrderCommand.Find.create(orderId, userId));

        return ApiResponse.success(OrderV1Dto.OrderResponse.from(orderInfo));
    }
}

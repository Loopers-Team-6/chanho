package com.loopers.interfaces.api.product;

import com.loopers.application.ProductFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductV1ApiController implements ProductV1ApiSpec {

    private final ProductFacade productFacade;

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductV1Dto.ProductInfo> getProduct(@PathVariable Long productId, HttpServletRequest request) {
        String userId = request.getHeader("X-USER-ID");
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID header is required.");
        }
        return ApiResponse.success(productFacade.getProduct(productId, Long.valueOf(userId)));
    }

    @GetMapping
    @Override
    public ApiResponse<PageResponse<ProductV1Dto.ProductInfo>> getProducts(Pageable pageable, HttpServletRequest request) {
        String userId = request.getHeader("X-USER-ID");
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID header is required.");
        }
        return ApiResponse.success(productFacade.getProducts(pageable, Long.valueOf(userId)));
    }

    @PostMapping("/{productId}/like")
    @Override
    public ApiResponse<Object> addLike(@PathVariable Long productId, HttpServletRequest request) {
        String userId = request.getHeader("X-USER-ID");
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID header is required.");
        }
        productFacade.addLike(userId, productId);

        return ApiResponse.success();
    }

    @DeleteMapping("/{productId}/like")
    @Override
    public ApiResponse<Object> removeLike(@PathVariable Long productId, HttpServletRequest request) {
        String userId = request.getHeader("X-USER-ID");
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID header is required.");
        }
        productFacade.removeLike(userId, productId);

        return ApiResponse.success();
    }
}

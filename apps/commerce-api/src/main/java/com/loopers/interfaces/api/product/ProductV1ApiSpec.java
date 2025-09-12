package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;

@Tag(name = "Product V1 API", description = "my-commerce 상품 API 입니다.")
public interface ProductV1ApiSpec {

    @Operation(summary = "상품 상세 조회")
    ApiResponse<ProductV1Dto.ProductInfo> getProduct(Long productId, HttpServletRequest request);

    @Operation(summary = "상품 목록 조회")
    ApiResponse<PageResponse<ProductV1Dto.ProductInfo>> getProducts(Pageable pageable, HttpServletRequest request);

    @Operation(summary = "상품 좋아요 추가")
    ApiResponse<Object> addLike(Long productId, HttpServletRequest request);

    @Operation(summary = "상품 좋아요 제거")
    ApiResponse<Object> removeLike(Long productId, HttpServletRequest request);
}

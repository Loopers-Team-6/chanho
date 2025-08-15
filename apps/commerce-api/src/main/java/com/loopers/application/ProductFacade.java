package com.loopers.application;

import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.interfaces.api.product.ProductV1Dto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;
    private final LikeService likeService;

    @Transactional(readOnly = true)
    @Cacheable(value = "product", key = "'user:' + #userId + ':product:' + #productId", unless = "#result == null")
    public ProductV1Dto.ProductInfo getProduct(Long productId, Long userId) {
        ProductEntity product = productService.findById(productId);
        boolean isLiked = likeService.isLiked(userId, productId);
        long likesCount = likeService.getLikesCount(productId);

        return ProductV1Dto.ProductInfo.of(product, likesCount, isLiked);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'user:' + #userId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize + ':sort:' + #pageable.sort", unless = "#result.isEmpty()")
    public Page<ProductV1Dto.ProductInfo> getProducts(Pageable pageable, Long userId) {
        Page<ProductEntity> products = productService.findAll(pageable);
        List<Long> productIds = products.getContent().stream()
                .map(ProductEntity::getId)
                .toList();

        Set<Long> likedProductIds = likeService.findLikedProductIds(userId, productIds);
        Map<Long, Long> likesCounts = likeService.getLikesCounts(productIds);


        return products.map(product -> {
            boolean isLiked = likedProductIds.contains(product.getId());
            long likesCount = likesCounts.getOrDefault(product.getId(), 0L);
            return ProductV1Dto.ProductInfo.of(product, likesCount, isLiked);
        });
    }
}

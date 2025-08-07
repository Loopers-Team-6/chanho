package com.loopers.domain.like;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public LikeEntity addLike(long userId, long productId) {
        return likeRepository.findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> {
                    UserEntity user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "사용자를 찾을 수 없습니다. userId: " + userId
                            ));
                    ProductEntity product = productRepository.findById(productId)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "상품을 찾을 수 없습니다. productId: " + productId
                            ));
                    LikeEntity newLike = LikeEntity.create(user, product);
                    return likeRepository.save(newLike);
                });
    }

    public void removeLike(long userId, long productId) {
        likeRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(LikeEntity::delete);
    }

    public boolean isLiked(Long userId, Long productId) {
        if (userId == null || productId == null) {
            throw new IllegalArgumentException("사용자 ID와 상품 ID는 null일 수 없습니다");
        }
        Optional<LikeEntity> like = likeRepository.findByUserIdAndProductId(userId, productId);
        return like.isPresent() && !like.get().isDeleted();
    }

    public Set<Long> findLikedProductIds(Long userId, List<Long> productIds) {
        if (userId == null || productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("사용자 ID와 상품 ID 목록은 null이거나 비어있을 수 없습니다");
        }

        return likeRepository.findLikedProductIdsByUserIdAndProductIds(userId, productIds);
    }

    public long getLikesCount(long productId) {
        return likeRepository.countByProductId(productId);
    }

    public Map<Long, Long> getLikesCounts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return likeRepository.findLikeCountsByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(LikeCountDto::productId, LikeCountDto::count));
    }

}

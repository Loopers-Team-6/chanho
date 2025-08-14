package com.loopers.domain.like;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public LikeEntity addLike(long userId, long productId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));
        ProductEntity product = productRepository.findByIdWithPessimisticLock(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. productId: " + productId));

        LikeEntity newLike = likeRepository.saveOrFind(LikeEntity.create(user, product));

        product.increaseLikeCount();
        productRepository.save(product);

        return newLike;
    }

    @Transactional
    public void removeLike(long userId, long productId) {
        likeRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(like -> {
                    like.delete();
                    likeRepository.save(like);

                    ProductEntity product = productRepository.findByIdWithPessimisticLock(productId)
                            .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. productId: " + productId));
                    product.decreaseLikeCount();
                    productRepository.save(product);
                });
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

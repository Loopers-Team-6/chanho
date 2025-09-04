package com.loopers.domain.like;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LikeEntity addLike(long userId, long productId) {
        LikeEntity like = likeRepository.findByUserIdAndProductId(userId, productId)
                // 값이 있는 경우 (이미 좋아요를 누른 적이 있음)
                .map(existLike -> {
                    // 삭제된 상태면 복원
                    if (existLike.isDeleted()) {
                        existLike.restore();
                    }
                    return existLike;
                })
                // 값이 없는 경우 생성 (처음으로 좋아요를 누름)
                .orElseGet(() -> {
                    UserEntity userProxy = userRepository.getReferenceById(userId);
                    ProductEntity productProxy = productRepository.getReferenceById(productId);

                    return LikeEntity.create(userProxy, productProxy);
                });

        likeRepository.save(like);
        eventPublisher.publishEvent(new LikeChangedEvent(productId, userId, true));

        return like;
    }

    @Transactional
    public void removeLike(long userId, long productId) {
        likeRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(like -> {
                    if (like.isDeleted()) {
                        return;
                    }
                    like.delete();
                    likeRepository.save(like);
                    eventPublisher.publishEvent(new LikeChangedEvent(productId, userId, false));
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

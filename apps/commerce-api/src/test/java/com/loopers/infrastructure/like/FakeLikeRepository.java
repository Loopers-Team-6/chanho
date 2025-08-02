package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeRepository;
import com.loopers.infrastructure.InMemoryCrudRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FakeLikeRepository extends InMemoryCrudRepository<LikeEntity> implements LikeRepository {

    @Override
    public long countAll() {
        return map.size();
    }

    @Override
    public long countByProductId(Long productId) {
        return map.values().stream()
                .filter(likeEntity -> likeEntity.getProduct().getId().equals(productId) && !likeEntity.isDeleted())
                .count();
    }

    @Override
    public Map<Long, Long> countByProductIds(List<Long> productIds) {
        return productIds.stream()
                .collect(Collectors.toMap(
                        productId -> productId,
                        this::countByProductId,
                        (existing, replacement) -> existing,
                        ConcurrentHashMap::new
                ));
    }

    @Override
    public Optional<LikeEntity> findByUserIdAndProductId(Long userId, Long productId) {
        return map.values().stream()
                .filter(likeEntity -> likeEntity.getUser().getId().equals(userId)
                        && likeEntity.getProduct().getId().equals(productId))
                .findFirst();
    }

    @Override
    public void deleteByUserIdAndProductId(Long userId, Long productId) {
        Optional<LikeEntity> like = findByUserIdAndProductId(userId, productId);
        like.ifPresent(LikeEntity::delete);
    }

    @Override
    public Set<Long> findLikedProductIdsByUserIdAndProductIds(Long userId, List<Long> productIds) {
        return map.values().stream()
                .filter(likeEntity -> likeEntity.getUser().getId().equals(userId)
                        && productIds.contains(likeEntity.getProduct().getId())
                        && !likeEntity.isDeleted())
                .map(likeEntity -> likeEntity.getProduct().getId())
                .collect(Collectors.toSet());
    }

}

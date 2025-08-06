package com.loopers.domain.like;

import com.loopers.domain.CustomCrudRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface LikeRepository extends CustomCrudRepository<LikeEntity> {

    long countAll();

    long countByProductId(Long productId);

    Map<Long, Long> countByProductIds(List<Long> productIds);

    Optional<LikeEntity> findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);

    Set<Long> findLikedProductIdsByUserIdAndProductIds(Long userId, List<Long> productIds);
}

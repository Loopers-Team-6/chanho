package com.loopers.domain.like;

import java.util.Optional;

public interface LikeRepository {

    int countAll();

    LikeEntity save(LikeEntity likeEntity);

    Optional<LikeEntity> findById(Long id);

    Optional<LikeEntity> findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}

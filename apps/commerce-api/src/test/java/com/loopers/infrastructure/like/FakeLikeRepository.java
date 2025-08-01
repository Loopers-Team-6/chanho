package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class FakeLikeRepository implements LikeRepository {

    private final Map<Long, LikeEntity> likeRepository = new ConcurrentHashMap<>();

    @Override
    public int countAll() {
        return likeRepository.size();
    }

    @Override
    public LikeEntity save(LikeEntity likeEntity) {
        LikeEntity like = likeRepository.putIfAbsent(likeEntity.getId(), likeEntity);
        return like == null ? likeEntity : like;
    }

    @Override
    public Optional<LikeEntity> findById(Long id) {
        return Optional.ofNullable(likeRepository.get(id));
    }

    @Override
    public Optional<LikeEntity> findByUserIdAndProductId(Long userId, Long productId) {
        return likeRepository.values().stream()
                .filter(likeEntity -> likeEntity.getUser().getId().equals(userId)
                        && likeEntity.getProduct().getId().equals(productId))
                .findFirst();
    }

    @Override
    public void deleteByUserIdAndProductId(Long userId, Long productId) {
        Optional<LikeEntity> like = findByUserIdAndProductId(userId, productId);
        like.ifPresent(LikeEntity::delete);
    }
}

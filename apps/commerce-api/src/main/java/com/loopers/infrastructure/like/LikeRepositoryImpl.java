package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeCountDto;
import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeRepository;
import com.loopers.infrastructure.AbstractRepositoryImpl;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class LikeRepositoryImpl extends AbstractRepositoryImpl<LikeEntity, LikeJpaRepository> implements LikeRepository {
    public LikeRepositoryImpl(LikeJpaRepository jpaRepository) {
        super(jpaRepository);
    }

    @Override
    public long countByProductId(Long productId) {
        return jpaRepository.countByProductId(productId);
    }

    @Override
    public List<LikeCountDto> findLikeCountsByProductIds(List<Long> productIds) {
        return jpaRepository.findLikeCountsByProductIds(productIds);
    }

    @Override
    public Optional<LikeEntity> findByUserIdAndProductId(Long userId, Long productId) {
        return jpaRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public void deleteByUserIdAndProductId(Long userId, Long productId) {
        findByUserIdAndProductId(userId, productId)
                .ifPresent(LikeEntity::delete);
    }

    @Override
    public Set<Long> findLikedProductIdsByUserIdAndProductIds(Long userId, List<Long> productIds) {
        return jpaRepository.findLikedProductIdsByUserIdAndProductIds(userId, productIds);
    }

    @Override
    public LikeEntity saveOrFind(LikeEntity likeEntity) {
        try {
            return jpaRepository.save(likeEntity);
        } catch (DataIntegrityViolationException e) {
            return jpaRepository
                    .findByUserIdAndProductId(likeEntity.getUser().getId(), likeEntity.getProduct().getId())
                    .orElseThrow(() -> new IllegalStateException("데이터 중복 예외 후 조회에 실패했습니다."));
        }
    }
}

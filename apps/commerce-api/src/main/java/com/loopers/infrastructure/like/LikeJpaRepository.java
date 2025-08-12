package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeCountDto;
import com.loopers.domain.like.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LikeJpaRepository extends JpaRepository<LikeEntity, Long> {
    long countByProductId(Long productId);

    @Query("SELECT new com.loopers.domain.like.LikeCountDto(l.product.id, COUNT(l.id)) " +
            "FROM LikeEntity l " +
            "WHERE l.product.id IN :productIds AND l.deletedAt IS NULL " +
            "GROUP BY l.product.id")
    List<LikeCountDto> findLikeCountsByProductIds(@Param("productIds") List<Long> productIds);

    Optional<LikeEntity> findByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT l.product.id " +
            "FROM LikeEntity l " +
            "WHERE l.user.id = :userId " +
            "AND l.product.id IN :productIds " +
            "AND l.deletedAt IS NULL")
    Set<Long> findLikedProductIdsByUserIdAndProductIds(@Param("userId") Long userId, @Param("productIds") List<Long> productIds);
}

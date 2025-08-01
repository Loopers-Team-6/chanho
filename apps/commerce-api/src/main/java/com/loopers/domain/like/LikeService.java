package com.loopers.domain.like;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public LikeEntity addLike(long userId, long productId) {
        return likeRepository.findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> {
                    UserEntity user = userRepository.findById(userId)
                            .orElseThrow(() -> new CoreException(
                                    ErrorType.BAD_REQUEST,
                                    "사용자를 찾을 수 없습니다. userId: " + userId
                            ));
                    ProductEntity product = productRepository.findById(productId)
                            .orElseThrow(() -> new CoreException(
                                    ErrorType.BAD_REQUEST,
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
}

package com.loopers.application.ranking;// com/loopers/application/ranking/RankingFacade.java

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.ranking.RankingV1Dto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingFacade {

    private final StringRedisTemplate redisTemplate;
    private final ProductRepository productRepository; // 상품 정보 조회를 위함

    @Transactional(readOnly = true)
    public PageResponse<RankingV1Dto.ProductRankResponse> getDailyRanking(LocalDate date, Pageable pageable) {
        String key = "ranking:daily:" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        long start = pageable.getOffset();
        long end = start + pageable.getPageSize() - 1;

        Set<ZSetOperations.TypedTuple<String>> rankedProducts = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, start, end);

        if (rankedProducts == null || rankedProducts.isEmpty()) {
            return PageResponse.from(Page.empty(pageable));
        }

        List<Long> productIds = rankedProducts.stream()
                .map(tuple -> Long.valueOf(tuple.getValue()))
                .toList();
        Map<Long, ProductEntity> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

        AtomicLong rank = new AtomicLong(start + 1);
        List<RankingV1Dto.ProductRankResponse> responses = rankedProducts.stream()
                .map(tuple -> {
                    long currentRank = rank.getAndIncrement();
                    Long productId = Long.valueOf(tuple.getValue());
                    double score = tuple.getScore();
                    ProductEntity product = productMap.get(productId);
                    return RankingV1Dto.ProductRankResponse.of(currentRank, score, product);
                })
                .toList();

        Long total = redisTemplate.opsForZSet()
                .zCard(key);
        return PageResponse.from(new PageImpl<>(
                responses,
                pageable,
                total != null
                        ? total
                        : 0
        ));
    }
}

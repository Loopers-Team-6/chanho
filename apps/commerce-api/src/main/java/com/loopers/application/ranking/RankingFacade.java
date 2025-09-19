package com.loopers.application.ranking;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.ranking.MonthlyProductRanking;
import com.loopers.domain.ranking.RankingService;
import com.loopers.domain.ranking.WeeklyProductRanking;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
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
    private final ProductRepository productRepository;
    private final RankingService rankingService;

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

    @Transactional(readOnly = true)
    public PageResponse<RankingV1Dto.ProductRankResponse> getWeeklyRanking(LocalDate date, Pageable pageable) {
        // 해당 주의 시작일(월요일) 계산
        LocalDate weekStartDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Page<WeeklyProductRanking> weeklyRankings = rankingService.getWeeklyRanking(weekStartDate, pageable);

        if (weeklyRankings.isEmpty()) {
            return PageResponse.from(Page.empty(pageable));
        }

        List<Long> productIds = weeklyRankings.getContent().stream()
                .map(WeeklyProductRanking::getProductId)
                .toList();

        Map<Long, ProductEntity> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

        Page<RankingV1Dto.ProductRankResponse> responsePage = weeklyRankings.map(ranking -> {
            ProductEntity product = productMap.get(ranking.getProductId());
            long rank = pageable.getOffset() + weeklyRankings.getContent().indexOf(ranking) + 1;
            return RankingV1Dto.ProductRankResponse.of(rank, ranking.getScore(), product);
        });

        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<RankingV1Dto.ProductRankResponse> getMonthlyRanking(LocalDate date, Pageable pageable) {
        // 해당 월의 시작일 계산
        LocalDate monthStartDate = date.with(TemporalAdjusters.firstDayOfMonth());

        Page<MonthlyProductRanking> monthlyRankings = rankingService.getMonthlyRanking(monthStartDate, pageable);

        if (monthlyRankings.isEmpty()) {
            return PageResponse.from(Page.empty(pageable));
        }

        List<Long> productIds = monthlyRankings.getContent().stream()
                .map(MonthlyProductRanking::getProductId)
                .toList();

        Map<Long, ProductEntity> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

        Page<RankingV1Dto.ProductRankResponse> responsePage = monthlyRankings.map(ranking -> {
            ProductEntity product = productMap.get(ranking.getProductId());
            long rank = pageable.getOffset() + monthlyRankings.getContent().indexOf(ranking) + 1;
            return RankingV1Dto.ProductRankResponse.of(rank, ranking.getScore(), product);
        });

        return PageResponse.from(responsePage);
    }
}

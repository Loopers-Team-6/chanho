package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.WeeklyProductRanking;
import com.loopers.domain.ranking.WeeklyProductRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class WeeklyProductRankingRepositoryImpl implements WeeklyProductRankingRepository {

    private final WeeklyProductRankingJpaRepository weeklyProductRankingJpaRepository;

    @Override
    public Page<WeeklyProductRanking> findAllByWeekStartDateOrderByScoreDesc(
            LocalDate weekStartDate,
            Pageable pageable
    ) {
        return weeklyProductRankingJpaRepository.findAllByWeekStartDateOrderByScoreDesc(weekStartDate, pageable);
    }

    @Override
    public void deleteByWeekStartDate(LocalDate weekStartDate) {
        weeklyProductRankingJpaRepository.deleteByWeekStartDate(weekStartDate);
    }
}

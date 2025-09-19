package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MonthlyProductRanking;
import com.loopers.domain.ranking.MonthlyProductRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class MonthlyProductRankingRepositoryImpl implements MonthlyProductRankingRepository {

    private final MonthlyProductRankingJpaRepository monthlyProductRankingJpaRepository;

    @Override
    public Page<MonthlyProductRanking> findAllByMonthStartDateOrderByScoreDesc(
            LocalDate monthStartDate,
            Pageable pageable
    ) {
        return monthlyProductRankingJpaRepository.findAllByMonthStartDateOrderByScoreDesc(monthStartDate, pageable);
    }

    @Override
    public void deleteByMonthStartDate(LocalDate monthStartDate) {
        monthlyProductRankingJpaRepository.deleteByMonthStartDate(monthStartDate);
    }
}

package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final WeeklyProductRankingRepository weeklyProductRankingRepository;
    private final MonthlyProductRankingRepository monthlyProductRankingRepository;

    public Page<WeeklyProductRanking> getWeeklyRanking(LocalDate weekStartDate, Pageable pageable) {
        return weeklyProductRankingRepository.findAllByWeekStartDateOrderByScoreDesc(weekStartDate, pageable);
    }

    public Page<MonthlyProductRanking> getMonthlyRanking(LocalDate monthStartDate, Pageable pageable) {
        return monthlyProductRankingRepository.findAllByMonthStartDateOrderByScoreDesc(monthStartDate, pageable);
    }
}

package com.loopers.domain.ranking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface WeeklyProductRankingRepository {

    Page<WeeklyProductRanking> findAllByWeekStartDateOrderByScoreDesc(LocalDate weekStartDate, Pageable pageable);

    void deleteByWeekStartDate(LocalDate weekStartDate);
}

package com.loopers.domain.ranking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface MonthlyProductRankingRepository {

    Page<MonthlyProductRanking> findAllByMonthStartDateOrderByScoreDesc(LocalDate monthStartDate, Pageable pageable);

    void deleteByMonthStartDate(LocalDate monthStartDate);
}

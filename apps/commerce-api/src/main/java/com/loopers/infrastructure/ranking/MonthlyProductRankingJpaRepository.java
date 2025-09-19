package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MonthlyProductRanking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface MonthlyProductRankingJpaRepository extends JpaRepository<MonthlyProductRanking, Long> {

    Page<MonthlyProductRanking> findAllByMonthStartDateOrderByScoreDesc(LocalDate monthStartDate, Pageable pageable);

    void deleteByMonthStartDate(LocalDate monthStartDate);
}

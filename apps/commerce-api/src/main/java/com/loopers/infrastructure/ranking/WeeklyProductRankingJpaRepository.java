package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.WeeklyProductRanking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface WeeklyProductRankingJpaRepository extends JpaRepository<WeeklyProductRanking, Long> {

    Page<WeeklyProductRanking> findAllByWeekStartDateOrderByScoreDesc(LocalDate weekStartDate, Pageable pageable);

    void deleteByWeekStartDate(LocalDate weekStartDate);
}

package com.loopers.domain.ranking;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "mv_product_rank_weekly",
        indexes = @Index(
                name = "idx_weekly_ranking_start_date_score",
                columnList = "weekStartDate, score DESC"
        )
)
public class WeeklyProductRanking extends BaseEntity {

    private Long productId;
    private double score;
    private LocalDate weekStartDate;

    public WeeklyProductRanking(Long productId, double score, LocalDate weekStartDate) {
        this.productId = productId;
        this.score = score;
        this.weekStartDate = weekStartDate;
    }
}

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
        name = "mv_product_rank_monthly",
        indexes = @Index(
                name = "idx_monthly_ranking_start_date_score",
                columnList = "monthStartDate, score DESC"
        )
)
public class MonthlyProductRanking extends BaseEntity {

    private Long productId;
    private double score;
    private LocalDate monthStartDate;

    public MonthlyProductRanking(Long productId, double score, LocalDate monthStartDate) {
        this.productId = productId;
        this.score = score;
        this.monthStartDate = monthStartDate;
    }
}

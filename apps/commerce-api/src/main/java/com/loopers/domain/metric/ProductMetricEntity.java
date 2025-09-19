package com.loopers.domain.metric;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "product_metrics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_id_metric_date",
                        columnNames = {"product_id", "metric_date"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductMetricEntity extends BaseEntity {

    @Column(
            name = "product_id",
            nullable = false
    )
    private Long productId;

    @Column(
            name = "metric_date",
            nullable = false
    )
    private LocalDate metricDate;

    @Column(
            name = "view_count",
            nullable = false
    )
    private long viewCount = 0L;

    @Column(
            name = "like_count",
            nullable = false
    )
    private long likeCount = 0L;

    @Column(
            name = "order_count",
            nullable = false
    )
    private long orderCount = 0L;

    @Column(
            name = "order_amount",
            nullable = false,
            precision = 10
    )
    private BigDecimal orderAmount = BigDecimal.ZERO;

    private ProductMetricEntity(Long productId, LocalDate metricDate) {
        this.productId = productId;
        this.metricDate = metricDate;
    }

    public static ProductMetricEntity create(Long productId, LocalDate metricDate) {
        return new ProductMetricEntity(productId, metricDate);
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void addOrder(int quantity, BigDecimal price) {
        this.orderCount += quantity;
        this.orderAmount = this.orderAmount.add(price.multiply(new BigDecimal(quantity)));
    }
}

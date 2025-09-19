package com.loopers.config.batch;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public final class WeeklyProductMetricDto {

    private Long productId;
    private long totalViewCount;
    private long totalLikeCount;
    private long totalOrderCount;

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (WeeklyProductMetricDto) obj;
        return Objects.equals(this.productId, that.productId) &&
                this.totalViewCount == that.totalViewCount &&
                this.totalLikeCount == that.totalLikeCount &&
                this.totalOrderCount == that.totalOrderCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, totalViewCount, totalLikeCount, totalOrderCount);
    }

    @Override
    public String toString() {
        return "WeeklyProductMetricDto[" +
                "productId=" + productId + ", " +
                "totalViewCount=" + totalViewCount + ", " +
                "totalLikeCount=" + totalLikeCount + ", " +
                "totalOrderCount=" + totalOrderCount + ']';
    }
}

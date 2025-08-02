package com.loopers.interfaces.api.point;

import com.loopers.domain.point.PointEntity;

import java.math.BigDecimal;

public class PointV1Dto {
    public record PointResponse(
            BigDecimal amount
    ) {
        public static PointResponse from(PointEntity point) {
            return new PointResponse(
                    point.getAmount()
            );
        }
    }

    public record ChargeRequest(
            Long userId,
            BigDecimal amount
    ) {
    }
}

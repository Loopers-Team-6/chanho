package com.loopers.domain.point;

import java.math.BigDecimal;

public interface PointService {

    PointEntity save(PointEntity point);

    PointEntity findByUserId(Long userId);

    void deductPoints(Long userId, BigDecimal amount);

    PointEntity findByUserIdWithPessimisticLock(long userId);
}

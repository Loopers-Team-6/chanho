package com.loopers.domain.point;

import java.math.BigDecimal;

public interface PointService {

    PointEntity save(PointEntity point);

    PointEntity findByUserId(Long id);

    void deductPoints(Long id, BigDecimal totalPrice);

    PointEntity findByUserIdWithPessimisticLock(long userId);
}

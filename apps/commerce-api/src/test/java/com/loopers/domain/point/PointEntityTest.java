package com.loopers.domain.point;

import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.Assert.assertThrows;

public class PointEntityTest {

    /*
     * 포인트 엔티티 테스트
     * - [o] 0 이하의 정수로 포인트를 충전 시 실패한다.
     */

    @DisplayName("포인트를 충전할 때, ")
    @Nested
    class Charge {
        @DisplayName("0 이하의 정수로 포인트를 충전 시 실패한다.")
        @Test
        void failedToChargePoint_whenAmountIsZeroOrNegative() {
            PointEntity point = new PointEntity(UserEntity.create(
                    "testuser",
                    "test@test.com",
                    UserGender.MALE,
                    LocalDate.of(2000, 1, 1)
            ));
            assertThrows(IllegalArgumentException.class, () -> point.charge(BigDecimal.ZERO));
            assertThrows(IllegalArgumentException.class, () -> point.charge(BigDecimal.valueOf(-1L)));
        }
    }
}

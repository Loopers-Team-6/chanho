package com.loopers.domain.point;

import com.loopers.application.user.UserFacade;
import com.loopers.domain.user.UserGender;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

@SpringBootTest
public class PointServiceIntegrationTest {

    /*
     * 포인트 조회 통합 테스트
     * - [o]  해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.
     * - [o]  해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.
     *
     * 포인트 충전 통합 테스트
     * - [o] 존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.
     */

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private PointService pointService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private UserService userService;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("포인트 조회")
    @Nested
    class GetPoints {
        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void returnsPoints_whenUserExists() {
            // arrange
            UserV1Dto.UserResponse saved = userFacade.signUp(new UserV1Dto.SignupRequest(
                    "mwma91",
                    "test@gmail.com",
                    UserGender.M,
                    LocalDate.of(2000, 1, 1)
            ));

            // act
            PointEntity point = pointService.findByUserId(saved.id());

            // assert
            assertThat(point).isNotNull();
            assertThat(point.getAmount()).isEqualTo(BigDecimal.ZERO);
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void returnsNull_whenUserDoesNotExist() {
            // arrange
            Long nonExistentUserId = 999L;

            // act
            PointEntity point = pointService.findByUserId(nonExistentUserId);

            // assert
            assertThat(point).isNull();
        }
    }

    @DisplayName("포인트 충전")
    @Nested
    class Charge {
        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
        @Test
        void fails_whenUserDoesNotExist() {
            // arrange
            Long nonExistentUserId = 999L;
            BigDecimal chargeAmount = BigDecimal.valueOf(100);

            // act & assert
            assertThrows(CoreException.class, () -> {
                userFacade.charge(nonExistentUserId, chargeAmount);
            });
        }
    }
}

package com.loopers.interfaces.api.point;

import com.loopers.application.user.UserFacade;
import com.loopers.domain.user.UserGender;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PointV1ApiE2ETest {

    /*
     * 포인트 조회 E2E 테스트
     * - [o]  포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.
     * - [o]  `X-USER-ID` 헤더가 없을 경우, `400 Bad Request` 응답을 반환한다.
     *
     * 포인트 충전 E2E 테스트
     * - [o]  존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.
     * - [o]  존재하지 않는 유저로 요청할 경우, `404 Not Found` 응답을 반환한다.
     */

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("GET /api/v1/points/{id}")
    @Nested
    class GetPoints {

        private static final Function<Long, String> ENDPOINT = userId -> "/api/v1/points/" + userId;

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
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-USER-ID", saved.id().toString());

            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                    testRestTemplate.exchange(ENDPOINT.apply(saved.id()), HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().amount()).isEqualTo(BigDecimal.ZERO)
            );
        }

        @DisplayName("`X-USER-ID` 헤더가 없을 경우, `400 Bad Request` 응답을 반환한다.")
        @Test
        void returnsBadRequest_whenUserDoesNotExist() {
            // arrange
            UserV1Dto.UserResponse saved = userFacade.signUp(new UserV1Dto.SignupRequest(
                    "mwma91",
                    "test@gmail.com",
                    UserGender.M,
                    LocalDate.of(2000, 1, 1)
            ));
            // act
            HttpHeaders httpHeaders = new HttpHeaders();
            // httpHeaders.add("X-USER-ID", saved.getUsername()); // 헤더를 추가하지 않음

            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                    testRestTemplate.exchange(ENDPOINT.apply(saved.id()), HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL),
                    () -> assertThat(response.getBody().data()).isNull()
            );
        }
    }

    @DisplayName("POST /api/v1/points/{id}")
    @Nested
    class Charge {
        private static final String ENDPOINT = "/api/v1/points";

        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        void returnsChargedPoints_whenUserExists() {
            // arrange
            UserV1Dto.UserResponse saved = userFacade.signUp(new UserV1Dto.SignupRequest(
                    "mwma91",
                    "test@gmail.com",
                    UserGender.M,
                    LocalDate.of(2000, 1, 1)
            ));

            String chargeAmount = """
                    {
                        "userId": %d,
                        "amount": %d
                    }
                    """.formatted(saved.id(), 1000);

            // act
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-USER-ID", saved.id().toString());
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(chargeAmount, httpHeaders), responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().amount()).isEqualTo(BigDecimal.valueOf(1000))
            );
        }

        @DisplayName("존재하지 않는 유저로 요청할 경우, `404 Not Found` 응답을 반환한다.")
        @Test
        void returnsNotFound_whenUserDoesNotExist() {
            // arrange
            String chargeAmount = """
                    {
                        "userId": %d,
                        "amount": %d
                    }
                    """.formatted(999L, 1000);

            // act
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-USER-ID", "999"); // 존재하지 않는 유저 ID
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(chargeAmount, httpHeaders), responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL),
                    () -> assertThat(response.getBody().data()).isNull()
            );
        }
    }
}

package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserGender;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserV1ApiE2ETest {

    /*
     * 회원가입 E2E 테스트
     * - [o]  회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.
     * - [o]  회원 가입 시에 성별이 없을 경우, `400 Bad Request` 응답을 반환한다.
     *
     * 내 정보 조회 E2E 테스트
     * - [o]  내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.
     * - [o]  존재하지 않는 ID 로 조회할 경우, `404 Not Found` 응답을 반환한다.
     */

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/users")
    @Nested
    class Signup {

        private static final String ENDPOINT = "/api/v1/users";

        @DisplayName("회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfo_whenSignupIsSuccessful() {
            // arrange
            String signupRequest = """
                    {
                        "username": "mwma91",
                        "email": "test@gmail.com",
                        "gender": "M",
                        "birth": "2000-01-01"
                    }
                    """;

            // act
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(signupRequest, httpHeaders), responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().username()).isEqualTo("mwma91"),
                    () -> assertThat(response.getBody().data().gender()).isEqualTo(UserGender.M)
            );
        }

        @DisplayName("회원 가입 시 요청 값이 올바르지 않을 경우 `400 Bad Request` 응답을 반환한다.")
        @ParameterizedTest(name = "{index} - {0}")
        @NullAndEmptySource
        @MethodSource("com.loopers.interfaces.api.user.param.SignupRequestParam#invalidRequests")
        void returnsBadRequest_whenParameterIsInvalid(String testName, String requestBody, HttpStatus expectedStatus) {
            // arrange
            // act
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(requestBody, httpHeaders), responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(expectedStatus),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL),
                    () -> assertThat(response.getBody().data()).isNull()
            );
        }
    }

    @DisplayName("GET /api/v1/users/me")
    @Nested
    class Me {

        private static final String ENDPOINT = "/api/v1/users/me";

        @DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfo_whenGetMyInfoIsSuccessful() {
            // arrange
            UserV1Dto.UserResponse created = signUpUser("mwma91", "test@gmail.com", "M", "2000-01-01");

            // act
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-USER-ID", String.valueOf(created.id()));

            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(httpHeaders), responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().username()).isEqualTo(created.username()),
                    () -> assertThat(response.getBody().data().gender()).isEqualTo(created.gender())
            );
        }

        private UserV1Dto.UserResponse signUpUser(String name, String email, String gender, String birth) {
            String signupRequest = """
                    {
                        "username": "%s",
                        "email": "%s",
                        "gender": "%s",
                        "birth": "%s"
                    }
                    """.formatted(name, email, gender, birth);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange("/api/v1/users", HttpMethod.POST, new HttpEntity<>(signupRequest, httpHeaders), responseType);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().data()).isNotNull();

            return response.getBody().data();
        }

        @DisplayName("존재하지 않는 ID 로 조회할 경우, `404 Not Found` 응답을 반환한다.")
        @Test
        void returnsNotFound_whenUserIdDoesNotExist() {
            // arrange
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-USER-ID", "9999"); // 존재하지 않는 ID

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(httpHeaders), responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL),
                    () -> assertThat(response.getBody().data()).isNull()
            );
        }

    }
}

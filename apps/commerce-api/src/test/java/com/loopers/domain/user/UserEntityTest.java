package com.loopers.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserEntityTest {

    /*
     * - [o]  ID 가 `영문 및 숫자 10자 이내` 형식에 맞지 않으면, UserEntity 객체 생성에 실패한다.
     * - [o]  이메일이 `xx@yy.zz` 형식에 맞지 않으면, UserEntity 객체 생성에 실패한다.
     * - [o]  생년월일이 `yyyy-MM-dd` 형식에 맞지 않으면, UserEntity 객체 생성에 실패한다.
     */

    @DisplayName("사용자 모델을 생성할 때, ")
    @Nested
    class Create {
        @DisplayName("username이 `영문 및 숫자 10자 이내` 형식에 맞지 않으면, 객체 생성에 실패한다")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"한글", "kor한글", "verylongtext", "_", "hello-world"})
        void failedToCreateUserModel_whenUsernameIsInvalid(String invalidUsername) {
            assertThrows(IllegalArgumentException.class,
                    () -> UserEntity.create(
                            invalidUsername,
                            "test@gmail.com",
                            UserGender.M,
                            LocalDate.of(2000, 1, 1)));
        }

        @DisplayName("이메일이 `xx@yy.zz` 형식에 맞지 않으면, UserEntity 객체 생성에 실패한다.")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"invalid-invalidEmail", "test@test", "test@test.", "test@.com", "@test.com", "test@test.c", "test@test_com", "test@test,com"})
        void failedToCreateUserModel_whenEmailIsInvalid(String invalidEmail) {
            assertThrows(IllegalArgumentException.class,
                    () -> UserEntity.create(
                            "mwma91",
                            invalidEmail,
                            UserGender.M,
                            LocalDate.of(2000, 1, 1)));
        }

        @DisplayName("생년월일이 `yyyy-MM-dd` 형식에 맞지 않거나 유효하지 않은 날짜이면, UserEntity 객체 생성에 실패한다.")
        @ParameterizedTest
        @NullSource
        @MethodSource("invalidBirthDates")
        void failedToCreateUserModel_whenBirthDateIsInvalid(LocalDate invalidBirthDate) {
            assertThrows(IllegalArgumentException.class,
                    () -> UserEntity.create(
                            "mwma91",
                            "test@gmail.com",
                            UserGender.M,
                            invalidBirthDate));
        }

        public static Stream<Arguments> invalidBirthDates() {
            return Stream.of(
                    Arguments.of(LocalDate.now().plusDays(1)), // Future date
                    Arguments.of(LocalDate.of(1000, 1, 1)) // Invalid date (year too early)
            );
        }
    }
}

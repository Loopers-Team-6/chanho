package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.time.LocalDate;

public class UserV1Dto {
    public record SignupRequest(
            String username,
            String email,
            UserGender gender,
            LocalDate birth
    ) {
        public SignupRequest {
            Validator.validateUsername(username);
            Validator.validateEmail(email);
            Validator.validateGender(gender);
            Validator.validateBirth(birth);
        }
    }

    public record UserResponse(
            Long id,
            String username,
            String email,
            UserGender gender,
            LocalDate birth
    ) {
        public static UserResponse from(UserEntity user) {
            return new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getGender(),
                    user.getBirth()
            );
        }
    }

    static class Validator {
        private static final String USERNAME_REGEX = "^[A-Za-z0-9]{1,10}$";
        private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

        static void validateUsername(String username) {
            if (username == null || username.isBlank() || !username.matches(USERNAME_REGEX)) {
                throw new CoreException(ErrorType.BAD_REQUEST, "이름은 1~10자의 영문 대소문자 또는 숫자만 포함할 수 있습니다.");
            }
        }

        static void validateEmail(String email) {
            if (email == null || email.isBlank() || !email.matches(EMAIL_REGEX)) {
                throw new CoreException(ErrorType.BAD_REQUEST, "이메일 형식이 올바르지 않습니다.");
            }
        }

        static void validateGender(UserGender gender) {
            if (gender == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "성별은 필수 입력입니다.");
            }
        }

        static void validateBirth(LocalDate birth) {
            if (birth == null || birth.isAfter(LocalDate.now()) || birth.isBefore(LocalDate.of(1900, 1, 1))) {
                throw new CoreException(ErrorType.BAD_REQUEST, "생일은 1900년 이후 날짜여야 하며, 오늘 이전이어야 합니다.");
            }
        }
    }
}

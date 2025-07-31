package com.loopers.interfaces.api.user.param;

import io.swagger.v3.core.util.Json;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SignupRequestParam {
    public static Stream<Arguments> invalidRequests() {
        return Stream.of(invalidUsernames(), invalidEmails(), invalidGenders(), invalidBirths())
                .flatMap(s -> s);
    }

    private static Stream<Arguments> invalidUsernames() {
        List<String> invalidParams = Arrays.asList(
                null, "", " ", "한글", "kor한글", "verylongtext", "_", "hello-world", "12345678901");
        return invalidParams.stream()
                .map(username -> invalidUsername("username: [%s]".formatted(username), username));
    }

    private static Stream<Arguments> invalidEmails() {
        List<String> invalidParams = Arrays.asList(
                null, "", " ", "invalid-email", "test@test", "test@test.", "test@.com",
                "@test.com", "test@test.c", "test@test_com", "test@test,com", "test@@email",
                "test@email.commmmmmmm", "test@email.ㅁㅁㅁ");
        return invalidParams.stream()
                .map(email -> invalidEmail("email: [%s]".formatted(email), email));
    }

    private static Stream<Arguments> invalidGenders() {
        List<String> invalidParams = Arrays.asList(
                null, "", " ", "0", "123", "남자", "x", "X", "ABC");
        return invalidParams.stream()
                .map(gender -> invalidGender("gender: [%s]".formatted(gender), gender));
    }

    private static Stream<Arguments> invalidBirths() {
        List<String> invalidParams = Arrays.asList(
                null, "", " ", "2000-01", "2000/01/01", "2000-1-1", "2000-01-1", "2000-1-01",
                "2000.01.01", "01-01-2000", "2000-13-01", "2000-01-32", "2023-02-29",
                "20000101", "000101", "00-01-01", "0001-01-01", "9999-01-01", "1899-01-01");
        return invalidParams.stream()
                .map(birth -> invalidBirth("birth: [%s]".formatted(birth), birth));
    }

    private static Arguments invalidUsername(String description, String username) {
        return Arguments.of(
                description,
                JsonBuilder.build()
                        .username(username)
                        .buildJson(),
                HttpStatus.BAD_REQUEST);
    }

    private static Arguments invalidEmail(String description, String email) {
        return Arguments.of(
                description,
                JsonBuilder.build()
                        .email(email)
                        .buildJson(),
                HttpStatus.BAD_REQUEST);
    }

    private static Arguments invalidGender(String description, String gender) {
        return Arguments.of(
                description,
                JsonBuilder.build()
                        .gender(gender)
                        .buildJson(),
                HttpStatus.BAD_REQUEST);
    }

    private static Arguments invalidBirth(String description, String birth) {
        return Arguments.of(
                description,
                JsonBuilder.build()
                        .birth(birth)
                        .buildJson(),
                HttpStatus.BAD_REQUEST);
    }

    static class JsonBuilder {
        private String username = "test";
        private String email = "test@gmail.com";
        private String gender = "M";
        private String birth = "2000-01-01";

        static JsonBuilder build() {
            return new JsonBuilder();
        }

        JsonBuilder username(String username) {
            this.username = username;
            return this;
        }

        JsonBuilder email(String email) {
            this.email = email;
            return this;
        }

        JsonBuilder gender(String gender) {
            this.gender = gender;
            return this;
        }

        JsonBuilder birth(String birth) {
            this.birth = birth;
            return this;
        }

        String buildJson() {
            return Json.pretty(Json.mapper().createObjectNode()
                    .put("username", username)
                    .put("email", email)
                    .put("gender", gender)
                    .put("birth", birth));
        }
    }
}

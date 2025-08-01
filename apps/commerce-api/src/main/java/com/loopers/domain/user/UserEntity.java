package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserEntity extends BaseEntity {

    @Column(unique = true, nullable = false, length = 10, name = "username")
    private String username;
    @Column(unique = true, nullable = false, length = 50, name = "email")
    private String email;
    @Column(nullable = false, name = "gender")
    private UserGender gender;
    @Column(nullable = false, name = "birth")
    private LocalDate birth;

    private UserEntity(String username, String email, UserGender gender, LocalDate birth) {
        UserValidator.validateUsername(username);
        UserValidator.validateEmail(email);
        UserValidator.validateBirth(birth);

        this.username = username;
        this.email = email;
        this.gender = gender;
        this.birth = birth;
    }

    public static UserEntity create(String username, String email, UserGender gender, LocalDate birth) {
        return new UserEntity(username, email, gender, birth);
    }

    public static UserEntity create(String username, String email, UserGender gender, String birth) {
        return new UserEntity(username, email, gender, LocalDate.parse(birth));
    }

    static class UserValidator {
        private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9]{1,10}$");
        private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

        static void validateUsername(String username) {
            if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
                throw new IllegalArgumentException("Invalid username");
            }
        }

        static void validateEmail(String email) {
            if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("Invalid email");
            }
        }

        static void validateBirth(LocalDate birth) {
            if (birth == null) {
                throw new IllegalArgumentException("Invalid birth: birth cannot be null");
            }

            try {
                if (birth.isAfter(LocalDate.now())) {
                    throw new IllegalArgumentException("birth cannot be in the future");
                }
                if (birth.isBefore(LocalDate.of(1900, 1, 1))) {
                    throw new IllegalArgumentException("birth cannot be before 1900");
                }
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid birth: must be in yyyy-MM-dd format and a valid date", e);
            }
        }
    }
}

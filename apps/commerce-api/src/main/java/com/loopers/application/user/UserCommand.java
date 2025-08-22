package com.loopers.application.user;

import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserCommand {
    public record Create(
            String username,
            String email,
            UserGender gender,
            LocalDate birth
    ) {

        public UserEntity toEntity() {
            return UserEntity.create(
                    username,
                    email,
                    gender,
                    birth
            );
        }
    }
}

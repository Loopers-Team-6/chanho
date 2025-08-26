package com.loopers.domain.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.loopers.util.EnumUtils;

public enum UserGender {
    MALE,
    FEMALE;

    @JsonValue
    public String getValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static UserGender fromValue(String value) {
        return EnumUtils.from(UserGender.class, value);
    }
}

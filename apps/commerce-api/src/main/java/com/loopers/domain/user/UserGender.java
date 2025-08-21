package com.loopers.domain.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.loopers.util.EnumUtils;

public enum UserGender {
    M("male"),
    F("female");

    private final String value;

    UserGender(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserGender fromValue(String value) {
        return EnumUtils.from(UserGender.class, value);
    }
}

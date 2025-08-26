package com.loopers.domain.order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.loopers.util.EnumUtils;

public enum OrderStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    FAILED;

    @JsonValue
    public String getValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static OrderStatus fromvalue(String value) {
        return EnumUtils.from(OrderStatus.class, value);
    }

}

package com.loopers.domain.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    POINT,
    CARD;

    @JsonCreator
    public static PaymentMethod fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }

        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.name().equalsIgnoreCase(value)) {
                return method;
            }
        }

        throw new IllegalArgumentException(value + " is not a valid value for PaymentMethod.");
    }

    @JsonValue
    public String getValue() {
        return name().toLowerCase();
    }
}

package com.loopers.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EnumUtils {

    public static <E extends Enum<E>> E from(Class<E> enumClass, String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Value cannot be null or empty.");
        }

        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for enum " + enumClass.getSimpleName() + ": " + value, e);
        }
    }
}

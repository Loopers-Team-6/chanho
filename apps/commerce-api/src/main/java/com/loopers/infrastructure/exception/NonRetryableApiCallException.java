package com.loopers.infrastructure.exception;

public class NonRetryableApiCallException extends RuntimeException {
    public NonRetryableApiCallException(Throwable cause) {
        super(cause);
    }
}
